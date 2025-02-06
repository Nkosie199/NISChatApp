NISChatApp

To run Java backend standalone:
```bash
1. cd src/main/java/com/mynger/mychatapp/nis
2. make
3. java Server.java
4. java Client.java
```

To run Spring application:
```bash
1. ./gradlew clean build
2. ./gradlew bootRun
```

To start the Server:
```bash
- curl -X POST "http://localhost:8080/server/start?port=4444"
- curl -X POST "http://localhost:8080/server/stop"
```


If you just want to embed a Bash terminal in a browser without using WebSockets, you can achieve this using ttyd (TTY daemon). It allows running a terminal session in a web page without needing WebSockets or custom backend code.

1. Install ttyd
```bash
On your Spring Boot server, install ttyd (supports Linux & macOS):
sudo apt install ttyd  # If available
# OR install manually
curl -LO https://github.com/tsl0922/ttyd/releases/latest/download/ttyd.x86_64
chmod +x ttyd.x86_64
sudo mv ttyd.x86_64 /usr/local/bin/ttyd
```

2. Start a Terminal Session
```bash
Run:
ttyd bash //This starts a web-based terminal accessible at http://localhost:7681/.

Optional: Run ttyd in the Background
To keep ttyd running:
nohup ttyd bash > /dev/null 2>&1 &
```

To kill a process bound to port:
```bash
1.1.1. netstat -ano | findstr :8080
or 
1.1.2. lsof -i :8080 (bash)
and
1.2.1. taskkill /PID <PID> /F (windows cmd prompt)
1.2.2. kill <PID>

or
2.1. kill -9 $(lsof -t -i:8080)
```