# endless-field

This self-motivated project is a Java backend framework implementing an intifite grid shared among multiple users. The field is lazy-generated, with fine-grained locking. Users fetch field areas (*chunks*) using HTTP API, and real-time updates distributed by websocket.

I built an MMO minesweeper game on top of this framework. Please have a look at [online demo](5.101.123.222:8080/online-minesweeper/game/0/).

Backend:
- Java 8
- JavaEE, ServletAPI
- MongoDB

Frontend:
- Javascript
- HTML5, Canvas
