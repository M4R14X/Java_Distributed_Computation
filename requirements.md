# Requirements – Java Distributed Computation

This document outlines the system, software, and configuration requirements needed to run the **Java_Distributed_Computation** project successfully.

---

## Software Requirements

- **Java Development Kit (JDK)**:  
  - Version: 8 or later  
  - Required on: All nodes (Client, Server, Slaves)

- **Operating System**:  
  - **Client & Server**: Windows  
  - **Slaves**: Alpine Linux (or any lightweight Linux distro)

- **Virtualization Platform** (for simulating distributed nodes):  
  - [VirtualBox](https://www.virtualbox.org/) or [VMware](https://www.vmware.com/)

---

## Hardware Requirements

- **CPU**: Multi-core (quad-core or better recommended)  
- **RAM**: Minimum 8 GB (12–16 GB preferred for running multiple VMs)  
- **Disk Space**: Minimum 10 GB free  

---

## Project Files

Ensure the following files are present in your project root:

- `Client.jar` – Java client interface  
- `Server.jar` – Server logic and worker handler  
- `Slave.jar` – Executable for each slave node  
- `operation.jar` – Contains matrix operation logic (e.g., addition)  
- `A.txt`, `B.txt` – Input matrix files  
- `confServer.txt` – Server IP and port (used by the client)  
- `confSlave.txt` – List of slave IPs and ports (used by the server)

### Example: `confSlave.txt`  
192.168.56.101:6001  
192.168.56.102:6002  
192.168.56.103:6003  
192.168.56.104:6004  
  
### Example: `confServer.txt`
127.0.0.1:5000  
  
## Network Configuration
  
- All nodes (host and VMs) must be on the same network using:
  - **Bridged Adapter** or **Host-Only Adapter** (in VirtualBox/VMware)

- Ensure ports (e.g., 5000, 6001–6004) are open on each VM’s firewall.

---

## How to Run

### 1. Start each Slave VM and run:
```bash
java -jar Slave.jar <slave-port>
```
Expected output:  
```Slave waiting on port <slave-port>```  
### 2. Run the Server:
```bash
java -jar Server.jar <server-port> confSlave.txt
```
Expected output:  
```Server is waiting for a connection on port <server-port>```
### 3. Run the Client:
```bash
java -jar Client.jar confServer.txt A.txt B.txt operation.jar
```
