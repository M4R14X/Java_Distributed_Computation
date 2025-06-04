# Java_Distributed_Computation
## Introduction
In a world where data processing is becoming increasingly demanding, the use of distributed systems helps reduce execution time by distributing tasks among multiple computing nodes. This project aims to design and implement a distributed system based on a client-server architecture, where computations are broken down and executed in parallel by multiple processing units. The main objective is to allow a client to submit a complex task to a server, which is responsible for dividing and distributing it to multiple workers. These workers then send subtasks to slaves, which execute the instructions and return the results to be merged before being sent back to the client.

## Technical Context
Matrix addition involves summing corresponding elements of two matrices of equal dimensions. While straightforward for small matrices, the operation becomes resource-intensive for large matrices (e.g., 1000x1000). A distributed approach addresses this by dividing the matrices into smaller blocks, processing them concurrently on separate machines, and combining the results.
The system leverages **Java sockets** for network communication, **threads** for concurrent task management, and **dynamic class loading** to execute operations from a .jar file.

Virtual machines (VMs) simulate a distributed environment:  
➔ Server and Client: Run on Windows for ease of development.  
➔ Slaves: Deployed on lightweight Alpine Linux VMs for efficiency.

## Workflow
**Client-Server Interaction:**  
The client connects to the server and transmits the matrices and operation file. The server assigns a unique task ID and stores the task in a stack.
Matrix Decomposition:
The server splits the input matrices into four equal-sized submatrices. For example, a 4x4 matrix is divided into four 2x2 submatrices.

**Task Distribution:**  
A worker thread handles all slaves sequentially. It retrieves the task from the stack, iterates over the list of slaves, and sends each submatrix pair to a corresponding slave. 
For instance:
Submatrix 1 → Slave 1
Submatrix 2 → Slave 2
Submatrix 3 → Slave 3
Submatrix 4 → Slave 4
The worker sends the submatrices and the operation .jar to each slave using Java object serialization.

**Slave Computation:**  
Each slave loads the operation (e.g., matrix addition) dynamically using Java reflection. It computes the result for its assigned submatrices and returns the output to the server.
Result Aggregation:
The worker collects results from all slaves and merges them into a single matrix. For example, four 2x2 sub-results are combined into a 4x4 final matrix.

**Result Aggregation:**  
The worker collects results from all slaves and merges them into a single matrix. For example, four 2x2 sub-results are combined into a 4x4 final matrix.

**Client Notification:**  
The server sends the final result back to the client, which displays it.

![sequence](https://github.com/user-attachments/assets/ec43290b-4fb9-4d15-96f6-3520306d7206)

The sequence diagram shows the step-by-step flow of tasks and results between the client, server, and slaves.
The client sends the matrices(A.txt, B.txt) and the operation .jar, to the server.
After receiving those files, the server, which includes an integrated worker, decomposes the matrices into smaller sub-matrices, and then sends each sub-matrix pair to a corresponding slave.
Each slave performs the computation on its assigned sub-matrices, and sends their results back to the server. The server then combines the results from all slaves and sends the final result back to the client.

The process is detailed in the points below: 

➔ **Client → Server**  
◆ Sends A.txt, B.txt, operation.jar.  
◆ Requests matrix computation.

➔ **Server → Worker Threads**  
◆ Stores task in queue.  
◆ Assigns task to a Worker.

➔ **Worker → Slaves**  
◆ Splits A.txt and B.txt into 4 parts.  
◆ Sends sub-matrices + operation.jar to different Slaves.

➔ **Slaves → Worker**  
◆ Slaves execute computation using operation.jar.  
◆ Send partial matrix results back to Worker.

➔ **Worker → Server**  
◆ Merges all partial results.  
◆ Returns the final computed matrix.

➔ **Server → Client**  
◆ Sends the final result to Client.

## Requirements and Setup  
For detailed prerequisites and setup instructions, see [requirements.md](./requirements.md).
