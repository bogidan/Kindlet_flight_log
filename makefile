NAME = Logbook
VER = 0.0.1

# Automated
CC = /opt/j2sdk4/bin/javac
JAR = /opt/j2sdk4/bin/jar
JARSIGN = /opt/j2sdk4/bin/jarsigner
TARGET = $(NAME).jar
AZW = $(NAME).azw2

all: $(TARGET) sign upload clean

build : $(TARGET) sign 
	
$(TARGET) : HAS_Tools
	echo "Description: Flight logging Application for Highland Aerosports" >> kindle.MF
	echo "Implementation-Title: APP: Logbook" >> kindle.MF
	echo "Implementation-Version: $(VER)" >> kindle.MF
	echo "Implementation-Vendor: Bob" >> kindle.MF
	echo "Amazon-Cover-Image: Glider.jpg" >> kindle.MF
	echo "Class-Path: /opt/amazon/ebook/lib/Kindlet-2.2.jar" >> kindle.MF
	echo "Main-Class: HAS_Tools.Logbook" >> kindle.MF
	echo "Extension-List: SDK" >> kindle.MF
	echo "SDK-Extension-Name: com.amazon.kindle.kindlet" >> kindle.MF
	#echo "SDK-Specification-Version: 2.1" >> kindle.MF
	echo "SDK-Specification-Version: 2.2" >> kindle.MF
	echo "Toolbar-Style: none" >> kindle.MF
	#echo "Toolbar-Mode: persistent" >> kindle.MF
	echo "Toolbar-Mode: transistent" >> kindle.MF
	echo "Font-Size-Mode: point" >> kindle.MF
	$(JAR) cmf kindle.MF $@ $</* Glider.jpg
	rm kindle.MF

HAS_Tools : *.java
	$(CC) -classpath ../Kindlet-2.2.jar -d ./ $<
	#$(CC) -classpath ../Kindlet-2.1.jar -d ./ $<

genkeys :
	keytool -genkeypair -keystore developer.keystore -storepass passwork -keypass password -alias dkBob -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" -validity 5300
	keytool -genkeypair -keystore developer.keystore -storepass passwork -keypass password -alias diBob -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" -validity 5300
	keytool -genkeypair -keystore developer.keystore -storepass passwork -keypass password -alias dnBob -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown" -validity 5300
	#keytool -importkeystore -srckeystore ../gpg_keys/developer.keystore -destkeystore developer.keystore
	scp developer.keystore  root@192.168.15.244:/var/local/java/keystore/developer.keystore
	mv developer.keystore /var/local/java/keystore/developer.keystore

send-keys :
	scp /var/local/java/keystore/developer.keystore  root@192.168.15.244:/var/local/java/keystore/developer.keystore

sign : $(TARGET)
	$(JARSIGN) -keystore /var/local/java/keystore/developer.keystore -storepass passwork -keypass password ./$(TARGET) dkBob
	$(JARSIGN) -keystore /var/local/java/keystore/developer.keystore -storepass passwork -keypass password ./$(TARGET) diBob
	$(JARSIGN) -keystore /var/local/java/keystore/developer.keystore -storepass passwork -keypass password ./$(TARGET) dnBob
	mv $(TARGET) $(AZW)

upload :
	scp $(AZW) root@192.168.15.244:/mnt/us/documents/

clean :
	rm $(AZW) HAS_Tools/*
	rmdir HAS_Tools
