# endless-field

Features
- [x] on field changing by someone's action, show who made this changes
  - [x] user + origin position in server action message
  - [x] layer with players labels
- [ ] users authorization / guest mode
- [ ] scores
- [ ] rankings

Core
- [ ] field.updateEntries() optimization
- [ ] unchecked cast in field.updateEntries()

Server
- [ ] don't resend same chunks on partial user's scope changing ([0,1] -> [1,2] - send chunk with id = 2) ([issue #2](https://github.com/zhukovsd/endless-field/issues/2))
- [ ] don't respond to action message if no cells were modified
- [ ] separate action message response for initiator and the rest of the clients (send score changes only to initiator, for example)
- [ ] statistics (server performance parameters)

Client
- [x] [resize events](https://github.com/zhukovsd/endless-field/issues/1)
- [ ] JSHint validation
- [ ] discard cells from actions messages, which does not belong to chunk, which has been loaded with /field request
- [ ] fix exception, throwing when local storage keys equal null

Testing
- [ ] jMeter websocket plugin (sessions in cookies + concurrent fix)

Documentation
- [ ] JavaDoc
- [ ] JSDoc