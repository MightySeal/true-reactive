## Let jobs finish
If host is stopped but not dead we might want to let our job finish, not drop it immediately.
For instance you show products list from cache and make backend sync in parallel.
The user picks an item from cache, but you still want that sync to happen, so you don't drop that request.
When the user goes back, he will see the updated list.