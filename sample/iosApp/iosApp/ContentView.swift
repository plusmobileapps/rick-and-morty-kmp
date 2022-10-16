//
//  ContentView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import SwiftUI
import rickandmortysdk

struct ContentView: View {

    @State var path: [Route] = []

    @State var selection = 1

    @StateObject
    var charactersBlocHolder = BlocHolder<CharactersBloc> { lifecycle in
        BlocBuilder.shared.createCharactersList(lifecycle: lifecycle)
    }

    @StateObject
    var episodesBlocHolder = BlocHolder<EpisodesBloc> { lifecycle in
        BlocBuilder.shared.createEpisodesBloc(lifecycle: lifecycle)
    }

    @StateObject
    var locationBlocHolder = BlocHolder<LocationBloc> { lifecycle in
        BlocBuilder.shared.createLocationsBloc(lifecycle: lifecycle)
    }

    var body: some View {
        let title = getTitle(selection: selection)

        NavigationStack(path: $path) {
            TabView(selection: $selection) {
                LazyView {
                    CharactersView(charactersBlocHolder.bloc)
                }
                        .onAppear {
                            LifecycleRegistryExtKt.resume(charactersBlocHolder.lifecycle)
                        }
                        .onDisappear {
                            LifecycleRegistryExtKt.pause(charactersBlocHolder.lifecycle)
                        }
                        .tabItem {
                            Image(systemName: "person.3")
                            Text("Characters")
                        }
                        .tag(1)

                LazyView {
                    EpisodesListView(episodesBlocHolder.bloc)
                            .onAppear {
                                LifecycleRegistryExtKt.resume(episodesBlocHolder.lifecycle)
                            }
                            .onDisappear {
                                LifecycleRegistryExtKt.pause(episodesBlocHolder.lifecycle)
                            }
                }
                        .tabItem {
                            Image(systemName: "list.triangle")
                            Text("Episodes")
                        }
                        .tag(2)

                LazyView {
                    LocationsListView(locationBlocHolder.bloc)
                            .onAppear {
                                LifecycleRegistryExtKt.resume(locationBlocHolder.lifecycle)
                            }
                            .onDisappear {
                                LifecycleRegistryExtKt.pause(locationBlocHolder.lifecycle)
                            }
                }
                        .tabItem {
                            Image(systemName: "map.circle.fill")
                            Text("Locations")
                        }
                        .tag(3)

                Text("About page")
                        .tabItem {
                            Image(systemName: "info.circle")
                            Text("About")
                        }
                        .tag(4)
            }
                    .navigationBarTitle(title)
                    .toolbar {
                        switch selection {
                        case 1:
                            NavigationLink(value: Route.characterSearch) {
                                Image(systemName: "magnifyingglass")
                            }
                        case 2:
                            NavigationLink(value: Route.episodeSearch) {
                                Image(systemName: "magnifyingglass")
                            }
                        default: EmptyView()
                        }

                    }
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case let .characterDetail(character):
                            CharacterDetailView(character)
                        case .characterSearch:
                            Text("Character search")
                                    .navigationBarTitle("Character Search", displayMode: .inline)
                        case let .epidodeDetail(id):
                            Text("Episode detail id: \(id)")
                        case .episodeSearch:
                            Text("Episode Search")
                        case let .locationDetail(id):
                            Text("Location detail id: \(id)")
                        }
                    }
        }
    }

    private func getTitle(selection: Int) -> String {
        switch selection {
        case 1:
            return "Characters"
        case 2:
            return "Episodes"
        case 3:
            return "Locations"
        default:
            return "About"
        }
    }

    private func onCharacterOutput(output: CharactersBlocOutput) {
        switch output {
        case let openCharacter as CharactersBlocOutput.OpenCharacter:
            path = [Route.characterDetail(openCharacter.character)]
        default:
            print("Not handled character output \(output.description)")
        }
    }
}

enum Route: Hashable {
    case characterDetail(RickAndMortyCharacter)
    case characterSearch
    case epidodeDetail(Int32)
    case episodeSearch
    case locationDetail(Int32)
}
