#!/usr/bin/env python3
################################################################################
# FILE: workstation/node_agent.py
# IDENTITY: Orbit Workstation Remote Node Agent
################################################################################
import os
import sys

# Re-export and execute the main daemon
root_daemon = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "orbit_node_daemon.py"))
if os.path.exists(root_daemon):
    with open(root_daemon) as f:
        code = compile(f.read(), root_daemon, 'exec')
        exec(code)
else:
    print("orbit_node_daemon.py not found in root.")
