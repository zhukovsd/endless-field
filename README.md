# endless-field

Features
- [ ] on field changing by someone's action, show who made this changes
  - [x] user + origin position in server action message
  - [ ] layer with players labels
    - [x] prototype
- [ ] users authorization / guest mode
- [ ] scores
- [ ] rankings

Core
- [ ] field.updateEntries() optimization
- [ ] unchecked cast in field.updateEntries()

Server
- [ ] don't resend same chunks on partial user's scope changing ([0,1] -> [1,2] - send chunk with id = 2)
- [ ] statistics (server performance parameters)
- [ ] loaded chunk cap, exceeding chunks removing

Client
- [ ] resize events
- [ ] JSHint validation
- [ ] buffered renderers
- [ ] layer has image data with bigger size than actual canvas. Don't perform any rendering on mouse mouse events, just scroll internal image data
- [ ] discard cells from actions messages, which does not belong to chunk, which has been loaded with /field request
- [ ] fix exception, throwing when local storage keys equal null

Testing
- [ ] jMeter websocket plugin (sessions in cookies + concurrent fix)

Documentation
- [ ] JavaDoc
- [ ] JSDoc