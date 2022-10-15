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
        NavigationStack(path: $path) {
            TabView {
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

                Text("About page")
                        .tabItem {
                            Image(systemName: "info.circle")
                            Text("About")
                        }
            }
                    .navigationBarTitle("Characters", displayMode: .inline)
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case let .characterDetail(id):
                            CharacterDetailView(id: id)
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

    private func onCharacterOutput(output: CharactersBlocOutput) {
        switch output {
        case let openCharacter as CharactersBlocOutput.OpenCharacter:
            path = [Route.characterDetail(openCharacter.character.id)]
        default:
            print("Not handled character output \(output.description)")
        }
    }
}

enum Route: Hashable {
    case characterDetail(Int32)
    case characterSearch
    case epidodeDetail(Int32)
    case episodeSearch
    case locationDetail(Int32)
}
