JCC = javac
JAVA = java
JFLAGS = -g
REMOTE_PEER_OPTS = -cp .:jsch.jar

default: compile

compile:
	$(JCC) $(JFLAGS) config/*.java
	$(JCC) $(JFLAGS) logging/*.java
	$(JCC) $(JFLAGS) msg/*.java
	$(JCC) $(JFLAGS) peerfunctions/*.java
	$(JCC) $(JFLAGS) serverfunctions/*.java
	$(JCC) $(JFLAGS) $(REMOTE_PEER_OPTS) StartRemotePeers.java

start-remote-peers: StartRemotePeers.class
	$(JAVA) $(REMOTE_PEER_OPTS) StartRemotePeers

clean:
	$(RM) *.class
	$(RM) config/*.class
	$(RM) logging/*.class
	$(RM) msg/*.class
	$(RM) peerfunctions/*.class
	$(RM) serverfunctions/*.class
	$(RM) *.log*
