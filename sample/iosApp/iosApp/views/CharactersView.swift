//
//  CharactersView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import SwiftUI
import rickandmortysdk

struct CharactersView : View {
    
    private let bloc: CharactersBloc
    
    @ObservedObject
    private var models: ObservableValue<CharactersBlocModel>
    
    init(_ bloc: CharactersBloc) {
        self.bloc = bloc
        models = ObservableValue(bloc.models)
    }
    
    var body: some View {
        let model = models.value
        
        if model.firstPageIsLoading {
            ProgressView()
        } else if model.pageLoadedError != nil && model.pageLoadedError!.isFirstPage {
            Text("Error loading the first page")
        } else {
            VStack {
                List {
                    ForEach(model.characters) { item in
                        NavigationLink(value: Route.characterDetail(item)) {
                            HStack {
                                AsyncImage(url: URL(string: item.imageUrl)) { image in
                                    image.resizable()
                                } placeholder: {
                                    ProgressView()
                                }.frame(width: 64, height: 64)
                                Spacer().frame(width: 16)
                                Text(item.name)
                            }
                        }
                    }
                    
                    if model.hasMoreToLoad || model.nextPageIsLoading {
                        ProgressView()
                            .onAppear(perform: bloc.loadMoreCharacters)
                    } else {
                        Text("End of List")
                    }
                    if model.pageLoadedError != nil {
                        Button(action: bloc.loadMoreCharacters) {
                            Text("Try again")
                        }
                    }
                }
            }
        }
    }
}

extension RickAndMortyCharacter : Identifiable {}
