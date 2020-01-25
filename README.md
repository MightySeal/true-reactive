# True Reactive

A way to write fully reactive android apps.

## Motivation

Android is an event driven system where application receives callbacks from activites/fragments, input sources, db/network request results, device state changes like GPS or connectivity. 
The reactive approach to data processing using RxJava made it much simpler to compose and transform these data streams. However, activity/fragment lifecycle is still an entry point and android app utilize UI-driven architecture approach. Handling lifecycle and configuration changes remains complex which leads to memory leaks, task interruptions, etc.
True reactive transforms Android UI lifecycle to a stream which can be composed with any other stream and provide a completely reactive way to organize app presentation layer.
  

## Key concepts

* Presenter survives configuration changes

## Key components

* `BaseFragment`/`BaseActivity` defines a set of input sources with `ViewEvents` and an output with a `render` method. ViewEvents and Data type to render and are generic parameters for every `BaseFragment`/`BaseActivity`.
Activity or Fragment extends base and also should implement `createPresenter` and `createViewHolder`.
`createPresenter` is called only once per every screen (per process)
`createViewHolder` is called every time the screen is recreated (navigate to system home and back, configuration change, etc.)

* ViewChannel is a reactive representation of a View for presenter.
As presenter survives config changes its lifecycle generally is longer than an activity or fragment instance. So, if you don't want to leak the context, presenter should not keep references to view after the view is destroyed. For Android this problem is usually solved with attach/detach methods for presenter.
`ViewChannel` allows to bind to view in reactive way instead. ViewChannel has a stream with current view state (Created, Resumed, Paused, etc.), and several functions to save/restore state, render data and select input sources from `ViewEvents`.

`ViewEvents` is generally a set of input sources like button clicks, text inputs and so on. Usually it should store Observables which can be subscribed by selecting them  with `viewEventsUntilDead` from `ViewChannel`.

Useful methods:

* `viewEventsUntilDead` selects an input source from `ViewChannel`. Keeps the stream alive across config changes in a safe way.
* `renderWhileAlive` sends specified data to the view `render` method. Re-delivers the latest available data when the view is recreated.
* `renderWhileActive` specified data to the view `render` method. Re-delivers the latest available data when the view is recreated, but doesn't keep the stream alive when the view is in stopped state.

## Example

### Define View
```kt
class SearchFragment : BaseFragment<SearchViewEvents, SearchState>() {

    override fun render(model: SearchState) {
        // render state, i.e. update adapter
    }

    override fun createPresenter(
            viewChannel: ViewChannel<SearchViewEvents, SearchState>,
            args: Bundle?,
            savedState: Bundle?
        ): BasePresenter {
        return SearchPresenter(viewChannel, SearchRepository())
    }

    override fun createViewHolder(view: View): SearchViewEvents {
        return SearchViewEvents(view)
    }
}

// Defines a model which the view can render 
data class SearchState(
    val searchResults: List<String>,
)

// Defines available inputs
class SearchViewEvents(view: View) {
    val searchInput: Observable<String> = view.searchView.textInput().share()
}
```

### Define Presenter
```kt
class SearchPresenter(
    private val channel: ViewChannel<SearchViewEvents, SearchState>,
    private val searchRepository: SearchRepository 
) : BasePresenter() {

    init {
        channel.viewEventsUntilDead { searchInput }
            .throttleLast(300, TimeUnit.MILLISECONDS)
            .startWith("")
            .flatMapSingle { searchQuery ->
                if (searchQuery.isNotBlank()) {
                    searchRepository.search(searchQuery)
                } else {
                    searchRepository.getPopular()
                }
            }
            .map { results -> SearchState(results) }
            .renderWhileAlive(channel)
    }
}
```
