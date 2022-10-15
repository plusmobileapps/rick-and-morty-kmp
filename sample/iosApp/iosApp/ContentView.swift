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

    let charactersBlocHolder: CharactersBlocHolder = CharactersBlocHolder()

    var body: some View {
        NavigationStack(path: $path) {
            TabView {
                LazyView {
                    CharactersView(charactersBlocHolder.bloc)
                            .onAppear {
                                LifecycleRegistryExtKt.resume(charactersBlocHolder.lifecycle)
                                charactersBlocHolder.setOutputListener { output in
                                    onCharacterOutput(output: output)
                                }
                            }
                            .onDisappear {
                                LifecycleRegistryExtKt.pause(charactersBlocHolder.lifecycle)
                            }
                }
                        .tabItem {
                            Image(systemName: "person.3")
                            Text("Characters")
                        }

                Text("Episodes List")
                        .tabItem {
                            Image(systemName: "list.triangle")
                            Text("Episodes")
                        }

                Text("Locations List")
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
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case let .characterDetail(id):
                            Text("id : \(id)")
                        case .characterSearch:
                            Text("Character search")
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
