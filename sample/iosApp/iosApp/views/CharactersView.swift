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
                        NavigationLink(value: Route.characterDetail(characterItem.value.id)) {
                            HStack {
                                Text(characterItem.value.name)
                                Text(characterItem.value.species)
                            }.onTapGesture {
                                bloc.onCharacterClicked(character: characterItem.value)
                            }
                        }
                    case let pageLoadingItem as CharactersListItem.PageLoading:
                        ProgressView()
                    default: EmptyView()
                    }
                }
            }
        }.navigationBarTitle("Characters", displayMode: .inline)
    }
}

extension CharactersListItem : Identifiable {}

class CharactersBlocHolder : ObservableObject {
    let lifecycle: LifecycleRegistry
    let bloc: CharactersBloc
    
    init() {
        lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        lifecycle.onCreate()
        bloc = BlocBuilder.shared.createCharactersList(lifecycle: lifecycle)
    }
    
    func setOutputListener(_ output: @escaping (CharactersBlocOutput) -> Void) {
        BlocBuilder.shared.charactersBlocOutput = output
    }
    
    deinit {
        lifecycle.onDestroy()
    }
}
