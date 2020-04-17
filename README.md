# distributed-bs

Repository to host Distributed Systems' [SDIS] first project. Made with [@cyrilico](https://github.com/cyrilico).

Work done for a subject in Distributed Systems [SDIS] in MIEIC @FEUP, in collaboration with [@cyrilico](https://github.com/cyrilico)

## Project: A Distributed Backup Service

Development of a distributed backup service for a local area network (LAN). The idea is to use the free disk space of the computers in a LAN for backing up files in other computers in the same LAN. The service is provided by servers in an environment that is assumed cooperative (rather than hostile). Nevertheless, each server retains control over its own disks and, if needed, may reclaim the space it made available for backing up other computers' files.

## Repository Structure

This repository has three main folders:
* __L01__: Code for the first labratory assignment.
* __L02__: Code for the second labratory assignment.
* __project__: Main project.

 
 ## Usage
 ```shell
 # compiling - output to bin directory
 sh compile.sh
 
 # starting a peer
 # sh run.sh <protocol version> <peer id> <service access point> [<MCReceiver address> <MCReceiver port> <MDBReceiver address> <MDBReceiver port> <MDRReceiver address> <MDRReceiver port>]
 sh run.sh 1.1 1 remote1
 
 # running a sample client
 # sh testapp.sh <remote host> <sub-protocol name> [<sub-protocol args]
 # example - running the RECLAIM protocol 
 sh testapp.sh //localhost:0/remote1 reclaim 100
 ```
 
 ## Usage Remarks

 - To kill possibly pendent executions, run `kill.sh`
 - OpenJDK based systems need to install openjfx
 - Peer metadata is saved in a `PeerController<ID>.ser` file and are loaded automatically once the same an ID is reused
