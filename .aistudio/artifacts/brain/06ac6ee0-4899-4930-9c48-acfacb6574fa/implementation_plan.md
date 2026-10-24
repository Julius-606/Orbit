# Implementation Plan: Offline Direct Link (Hotspot, USB Tethering & Bluetooth)

Provide a robust, zero-internet offline connection architecture allowing Pocket Orbit to control any local computer (Linux, Windows, or macOS) through Phone Mobile Hotspot, USB Tethering, or Bluetooth Serial (RFCOMM).

---

## 1. Architecture & Protocol Overview

```
┌────────────────────────────────────────────────────────┐
│            Pocket Orbit Android Application            │
└──────┬──────────────────────┬───────────────────┬──────┘
       │                      │                   │
       ▼                      ▼                   ▼
 [Mobile Hotspot]       [USB Tethering]     [Bluetooth RFCOMM]
 (192.168.43.x)         (192.168.42.x)      (UUID: 00001101-...)
       │                      │                   │
       └──────────────┬───────┴───────────────────┘
                      ▼
 ┌────────────────────────────────────────────────────────┐
 │       Universal Node Daemon (orbit_node_daemon.py)     │
 │        Works on Windows (PowerShell), Linux & macOS    │
 └────────────────────────────────────────────────────────┘
```

### Channel 1: Mobile Hotspot (Wi-Fi Tethering)
- Phone creates a Wi-Fi Hotspot (mobile data can remain turned off).
- Computer connects to the phone's Hotspot.
- Phone operates as gateway (usually `192.168.43.1`).
- Pocket Orbit discovers the computer by auto-scanning the active subnet on port `8888` / `8000`.

### Channel 2: USB Tethering
- Phone connected to computer via USB cable; user enables "USB Tethering" in Android settings.
- Android creates an RNDIS/CDC network interface (`rndis0` / `usb0`), establishing a high-speed, zero-interference point-to-point link (typically `192.168.42.x`).
- Auto-detected by Pocket Orbit through network interface introspection.

### Channel 3: Bluetooth Serial (RFCOMM / SPP)
- Completely network-free; requires no IP addresses, Wi-Fi, or cables.
- Standard Bluetooth Serial Port Profile (SPP, UUID `00001101-0000-1000-8000-00805F9B34FB`).
- Android uses `BluetoothSocket` to send commands and receive streaming execution responses.
- Python daemon runs a cross-platform Bluetooth RFCOMM listener.

---

## 2. Proposed Implementation Steps

### Phase 1: Android Manifest & Permissions
- Add runtime and install-time permissions in `app/src/main/AndroidManifest.xml`:
  - `BLUETOOTH`, `BLUETOOTH_ADMIN` (for Android <= 11)
  - `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` (for Android 12+)
  - `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`

### Phase 2: Offline Connectivity Core (`app/src/main/java/com/aistudio/weathertracker/kxmpzq/network/offline/`)
1. **`OfflineNetworkScanner.kt`**:
   - Enumerates network interfaces (`NetworkInterface.getNetworkInterfaces()`).
   - Identifies Hotspot (`ap0`, `wlan1`, `192.168.43.x`) and USB Tethering (`rndis0`, `usb0`, `192.168.42.x`) IP ranges.
   - Fast parallel TCP ping scanner across `1..254` on port `8888` and `8000` with 250ms timeouts (total scan completes in 1-2 seconds).
   - Emits discovered nodes with IP, OS, latency, and hostnames.
2. **`BluetoothNodeManager.kt`**:
   - Lists bonded/paired devices.
   - Manages RFCOMM connection lifecycle, background reading coroutine, and auto-reconnect.
   - Wraps commands and telemetry in lightweight JSON envelopes matching Orbit node protocols.

### Phase 3: Android UI & ViewModels
1. **`TerminalViewModel.kt`**:
   - Add state: `offlineScanState`, `discoveredNodes`, `pairedBtDevices`, `isScanningSubnet`.
   - Add actions: `scanTetheredSubnet()`, `connectOfflineNode()`, `connectBluetoothNode()`.
2. **`TerminalScreen.kt` - Node Management Sheet**:
   - Add **"Offline Direct Link"** segment in Node Sheet.
   - Section A: **Hotspot & USB Tethering Auto-Discovery**:
     - Visual detection indicator (e.g., "Active USB Interface Detected: 192.168.42.129" or "Hotspot Gateway: 192.168.43.1").
     - "Scan Offline Subnet" button with animated radar pulse.
     - List of discovered computers with 1-tap "Connect" button.
   - Section B: **Bluetooth Direct Link**:
     - Paired device picker (e.g., "fugitive-ThinkPad-Yoga", "MacBook-Pro").
     - 1-tap connect button.

### Phase 4: Python Daemon Enhancement (`orbit_node_daemon.py`)
- Enhance `orbit_node_daemon.py` with:
  1. Auto-detection and display of all local IP addresses (Ethernet, Wi-Fi, Hotspot, USB Tethering).
  2. Built-in Bluetooth RFCOMM listener mode (`--bt` or combined dual-mode) using Python's standard `socket.AF_BLUETOOTH` on Linux and Windows.
  3. Seamless JSON execution protocol matching REST `/execute` and `/status`.

---

## 3. Verification & Testing Plan
- **Subnet Scanner Test**: Verify subnet scanner correctly identifies active interfaces and scans `/24` subnets without blocking the main Compose UI.
- **Compilation**: Run `compile_applet` to ensure all Kotlin, Compose, and Android permission integrations compile cleanly.
- **Python Daemon Test**: Verify `orbit_node_daemon.py` compiles with `python3 -m py_compile` and runs cleanly with arguments.
