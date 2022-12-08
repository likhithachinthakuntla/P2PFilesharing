Project Members:
1. Likhitha Chinthakuntla (UFID:1172-3267)
2. Anil Kumar Kondapalli (UFID:4511-2505)
3. Tanmayi Kasthuri (UFID:1696-3063)

Demo link:
https://uflorida-my.sharepoint.com/:v:/g/personal/chinthakuntla_l_ufl_edu/ETI9CFJKTfFPgjmd9YAKZD8BvdR4CvA-AWqtvVAOZBlrGw?e=tQWiiQ

Problem Statement-P2P FileSharing:
1. Build a P2P file-sharing Java application by using BitTorrent protocol where one peer has a file initially and towards the end all the peers have that file which is to je judged by the bitfield of the peer by forming reliable TCP connection between peers
2. Share files with choking and unchoking mechanisms among peers while sending have, interested and uninterested messages.


Running steps:
1. We have tree.jpg file in 1001 folder.
2. We use the make command to compile all the java files.
3. Check that Common.cfg and PeerInfo.cfg have the correct parameters and are in the same folder as the source files, where the bitfields of PeerInfo.cfg are set to 0 except for the first peer and the values of Common.cfg are specified in the given program description.
4. Run the command make start-remote-peers to start the peer process.
5. make clean command is used to delete the class files after execution.

Process:
The starting order of peers is specified in the PeerInfo config file, and the peerProcess receives the peer ID as a parameter. The newly started peer is expected to establish a TCP connection with every peer that is going to be involved in file sharing and has started prior to it. In the PeerInfo file has  bits 0 and 1 where 1 the bit is updated to 1 once the whole file is received.
TCP Protocol: This project uses the TCP protocol to connect peers who would like to share files among themselves. In order to do so, the peers should first exchange a handshake message that encompasses the zero bits, peer ID and header Following that, a stream of data messages with message length, type, and payload is sent which is available in a variety of configurations.

Contribution:
1.  Likhitha Chinthakuntla - Handling of choked and unchoked messages and sending interested and uninterested messages ; Choosing a preferred neighbor and a hopeful unchoked neighbor
2. Anil Kumar Kondapalli - Tracking the status of other peers and terminating message listener threads and the program once all peers have downloaded the file;  Piece-by-piece file management
3. Tanmayi Kasthuri - Maintaining the config files and parameters. Socket and thread maintenance; Data reading from the socket. Maintaining sessions with credentials.
