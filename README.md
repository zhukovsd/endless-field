# endless-field

Features
- [ ] on field changing by someone's action, show who made this changes
  - [x] user + origin position in server action message
  - [ ] layer with players labels
    - [x] prototype

Server
- [ ] don't resend same chunks on partial user's scope changing ([0,1] -> [1,2] - send chunk with id = 2)
- [ ] field.updateEntries() optimization
- [ ] statistics (server performance parameters)

Client
- [ ] resize events
- [ ] JSHint validation
- [ ] buffered renderers
- [ ] layer has image data with bigger size than actual canvas. Don't perform any rendering on mouse mouse events, just scroll internal image data

Documentation [ ]
- [ ] JavaDoc
- [ ] JSDoc