Mynger Web Application


To run:
1. mvn clean install
2. mvn java:exec

To deploy:
heroku buildpacks:set heroku/java --app nis-chat-app

To check logs:
heroku logs --app=nis-chat-app --tail