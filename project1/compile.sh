# Compile
#rm -r -f *.class

#javac *.java server/*.java channel/*.java protocol/*.java receiver/*.java rmi/*.java storage/*.java test/*.java utils/*.java


rm -rf bin
mkdir -p bin
javac -d bin -sourcepath . test/TestApp.java server/Peer.java

