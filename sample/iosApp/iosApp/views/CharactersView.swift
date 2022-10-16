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
        
        return VStack {
            List {
                ForEach(model.listItems) { item in
                    switch item {
                    case let characterItem as CharactersListItem.Character:
                        NavigationLink(value: Route.characterDetail(characterItem.value)) {
                            HStack {
                                AsyncImage(url: URL(string: characterItem.value.imageUrl)) { image in
                                    image.resizable()
                                } placeholder: {
                                    ProgressView()
                                }.frame(width: 64, height: 64)
                                Spacer().frame(width: 16)
                                Text(characterItem.value.name)
                            }
                        }
                    case _ as CharactersListItem.PageLoading:
                        ProgressView()
                    default: EmptyView()
                    }
                }
            }
        }
    }
}

extension CharactersListItem : Identifiable {}
