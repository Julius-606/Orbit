import { neon } from '@neondatabase/serverless';
import dotenv from 'dotenv';
import dns from 'dns';

// Fix for Node.js Happy Eyeballs / IPv6 connection timeouts (ETIMEDOUT) on Linux/cloud
try {
  if (typeof dns.setDefaultResultOrder === 'function') {
    dns.setDefaultResultOrder('ipv4first');
  }
} catch {
  // Ignore fallback if runtime does not support
}

dotenv.config();

// Dedicated secret for DebateHub Neon DB, separate from Orbit's database URL
const databaseUrl =
  process.env.DEBATEHUB_NEON_DATABASE_URL ||
  process.env.DEBATEHUB_DATABASE_URL ||
  process.env.NEON_DATABASE_URL ||
  process.env.DATABASE_URL;

export const isNeonConfigured = Boolean(databaseUrl && databaseUrl.startsWith('postgres'));

// Export Neon SQL client if configured
export const sql = isNeonConfigured && databaseUrl ? neon(databaseUrl) : null;

/**
 * Initializes the required PostgreSQL schema in Neon.
 */
export async function initializeNeonTables(): Promise<{ success: boolean; message: string }> {
  if (!sql) {
    return {
      success: false,
      message: 'NEON_DATABASE_URL environment variable is not set. Add it in AI Studio Secrets or .env file.',
    };
  }

  try {
    // 1. Members table
    await sql`
      CREATE TABLE IF NOT EXISTS members (
        id VARCHAR(64) PRIMARY KEY,
        full_name VARCHAR(255) NOT NULL,
        student_id VARCHAR(64) NOT NULL,
        email VARCHAR(255) NOT NULL UNIQUE,
        phone VARCHAR(64),
        role VARCHAR(32) NOT NULL DEFAULT 'member',
        executive_position VARCHAR(128),
        year_of_study VARCHAR(32) NOT NULL DEFAULT 'Year 1',
        faculty VARCHAR(255) NOT NULL,
        membership_status VARCHAR(32) NOT NULL DEFAULT 'Pending',
        dues_amount_kes NUMERIC NOT NULL DEFAULT 500,
        mpesa_ref VARCHAR(64),
        joined_date VARCHAR(32) NOT NULL,
        attendance_rate NUMERIC NOT NULL DEFAULT 0,
        debates_attended_count INTEGER NOT NULL DEFAULT 0,
        total_debates_count INTEGER NOT NULL DEFAULT 0,
        speaker_points_avg NUMERIC NOT NULL DEFAULT 70,
        bio TEXT,
        alumni_occupation VARCHAR(255),
        alumni_organization VARCHAR(255),
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
    `;

    // 2. Financial Transactions table
    await sql`
      CREATE TABLE IF NOT EXISTS financial_transactions (
        id VARCHAR(64) PRIMARY KEY,
        date VARCHAR(32) NOT NULL,
        type VARCHAR(16) NOT NULL,
        category VARCHAR(64) NOT NULL,
        amount_kes NUMERIC NOT NULL,
        description TEXT NOT NULL,
        reference_code VARCHAR(64) NOT NULL,
        recorded_by VARCHAR(255) NOT NULL,
        status VARCHAR(32) NOT NULL DEFAULT 'Verified',
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
    `;

    // 3. Debate Sessions table
    await sql`
      CREATE TABLE IF NOT EXISTS debate_sessions (
        id VARCHAR(64) PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        motion TEXT NOT NULL,
        motion_info_slide TEXT,
        category VARCHAR(64) NOT NULL,
        format VARCHAR(64) NOT NULL,
        date VARCHAR(32) NOT NULL,
        time VARCHAR(64) NOT NULL,
        status VARCHAR(32) NOT NULL DEFAULT 'Scheduled',
        google_meet_link TEXT,
        winning_team VARCHAR(128),
        adjudicators JSONB DEFAULT '[]'::jsonb,
        teams JSONB DEFAULT '[]'::jsonb,
        summary_clashes JSONB DEFAULT '[]'::jsonb,
        attendee_ids JSONB DEFAULT '[]'::jsonb,
        transcript JSONB DEFAULT '[]'::jsonb,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
    `;

    // 4. Executive Agendas table
    await sql`
      CREATE TABLE IF NOT EXISTS executive_agendas (
        id VARCHAR(64) PRIMARY KEY,
        meeting_title VARCHAR(255) NOT NULL,
        date VARCHAR(32) NOT NULL,
        time VARCHAR(64) NOT NULL,
        location VARCHAR(255) NOT NULL,
        status VARCHAR(32) NOT NULL DEFAULT 'Upcoming',
        chairperson VARCHAR(255) NOT NULL,
        agenda_items JSONB DEFAULT '[]'::jsonb,
        logistics_checklist JSONB DEFAULT '[]'::jsonb,
        minutes_summary TEXT,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
    `;

    // 5. Announcements table
    await sql`
      CREATE TABLE IF NOT EXISTS announcements (
        id VARCHAR(64) PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        content TEXT NOT NULL,
        author VARCHAR(255) NOT NULL,
        author_role VARCHAR(128) NOT NULL,
        publish_date VARCHAR(32) NOT NULL,
        priority VARCHAR(32) NOT NULL DEFAULT 'Normal',
        category VARCHAR(64) NOT NULL,
        pinned BOOLEAN NOT NULL DEFAULT false,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
    `;

    // 6. Calendar Events table
    await sql`
      CREATE TABLE IF NOT EXISTS calendar_events (
        id VARCHAR(64) PRIMARY KEY,
        title VARCHAR(255) NOT NULL,
        date VARCHAR(32) NOT NULL,
        start_time VARCHAR(32) NOT NULL,
        end_time VARCHAR(32) NOT NULL,
        location VARCHAR(255) NOT NULL,
        google_meet_url TEXT,
        event_type VARCHAR(64) NOT NULL,
        description TEXT NOT NULL,
        lead_coordinator VARCHAR(255) NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
    `;

    // 7. Mentorship Notes table
    await sql`
      CREATE TABLE IF NOT EXISTS mentorship_notes (
        id VARCHAR(64) PRIMARY KEY,
        alumni_id VARCHAR(64) NOT NULL,
        alumni_name VARCHAR(255) NOT NULL,
        mentee_id VARCHAR(64) NOT NULL,
        mentee_name VARCHAR(255) NOT NULL,
        topic VARCHAR(255) NOT NULL,
        date VARCHAR(32) NOT NULL,
        advice_summary TEXT NOT NULL,
        status VARCHAR(32) NOT NULL DEFAULT 'Active',
        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
      );
    `;

    return {
      success: true,
      message: 'Neon PostgreSQL tables initialized successfully (members, transactions, debates, agendas, announcements, events, mentorship_notes).',
    };
  } catch (error: any) {
    console.error('Error initializing Neon tables:', error);
    return {
      success: false,
      message: `Failed to initialize Neon schema: ${error.message}`,
    };
  }
}

/**
 * Returns safe connection diagnostics for UI and logs.
 */
export function getNeonDiagnostics(): Record<string, any> {
  const raw = databaseUrl || '';
  let host = 'not_configured';
  let isPooler = false;
  let hasSsl = false;

  try {
    if (raw.startsWith('postgres')) {
      const parsed = new URL(raw.replace('postgresql://', 'http://').replace('postgres://', 'http://'));
      host = parsed.hostname;
      isPooler = host.includes('-pooler');
      hasSsl = parsed.searchParams.get('sslmode') === 'require';
    }
  } catch {
    // URL parse fallback
  }

  return {
    configured: isNeonConfigured,
    secretDetected: Boolean(databaseUrl),
    secretName: process.env.DEBATEHUB_NEON_DATABASE_URL ? 'DEBATEHUB_NEON_DATABASE_URL' : (process.env.NEON_DATABASE_URL ? 'NEON_DATABASE_URL' : 'DATABASE_URL'),
    host,
    isPoolerEndpoint: isPooler,
    hasSslModeRequire: hasSsl,
    maskedUrl: raw ? raw.replace(/:([^:@]+)@/, ':****@') : 'not set',
  };
}

/**
 * Actively probes the Neon PostgreSQL connection and returns detailed latency,
 * detected tables, and connection status.
 */
export async function testNeonConnection(): Promise<{
  ok: boolean;
  latencyMs: number;
  tables: string[];
  dbName?: string;
  error?: string;
  advice?: string;
}> {
  if (!sql) {
    return {
      ok: false,
      latencyMs: -1,
      tables: [],
      error: 'Neon DB URL is not set. Add DEBATEHUB_NEON_DATABASE_URL to Space Secrets.',
      advice: 'Ensure DEBATEHUB_NEON_DATABASE_URL is provided in Hugging Face Space Settings -> Secrets.',
    };
  }

  const start = Date.now();
  try {
    const probe = await sql`SELECT current_database() as db_name, version() as ver;`;
    const latencyMs = Date.now() - start;

    const tableRows = await sql`
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public' 
      ORDER BY table_name;
    `;
    const tables = tableRows.map((r: any) => r.table_name);

    // Auto-bootstrap tables if empty
    if (tables.length === 0 || !tables.includes('members')) {
      console.log('🔄 Neon connected but tables missing. Auto-bootstrapping schema...');
      await initializeNeonTables();
    }

    return {
      ok: true,
      latencyMs,
      tables,
      dbName: probe[0]?.db_name || 'neondb',
    };
  } catch (err: any) {
    const latencyMs = Date.now() - start;
    let advice = 'Check your connection string and internet connection.';
    if (err.message?.includes('ETIMEDOUT') || err.message?.includes('fetch failed')) {
      advice = 'Network timeout contacting Neon endpoint. Verify that the endpoint host exists and is active in neon.tech dashboard.';
    } else if (err.message?.includes('password') || err.message?.includes('authentication')) {
      advice = 'Authentication failed. Check username and password in DEBATEHUB_NEON_DATABASE_URL.';
    }
    return {
      ok: false,
      latencyMs,
      tables: [],
      error: err.message || String(err),
      advice,
    };
  }
}
