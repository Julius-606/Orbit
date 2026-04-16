"""add remarks to study tasks

Revision ID: 7a8b9c0d1e2f
Revises: 0252b66d5ffd
Create Date: 2026-04-15 12:00:00.000000

"""
from alembic import op
import sqlalchemy as sa

# revision identifiers, used by Alembic.
revision = '7a8b9c0d1e2f'
down_revision = '0252b66d5ffd'
branch_labels = None
depends_on = None

def upgrade():
    # Add remarks and is_reminder columns to study_tasks
    op.add_column('study_tasks', sa.Column('remarks', sa.Text(), nullable=True))
    op.add_column('study_tasks', sa.Column('is_reminder', sa.Boolean(), nullable=True, server_default='false'))

def downgrade():
    op.drop_column('study_tasks', 'is_reminder')
    op.drop_column('study_tasks', 'remarks')
