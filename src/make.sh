echo "cleaning *.class ..."
rm -f *.class
rm -rf ./*/*.class
echo "compiling Client.java"
javac Client.java
echo "compiling Server.java"
javac Server.java
