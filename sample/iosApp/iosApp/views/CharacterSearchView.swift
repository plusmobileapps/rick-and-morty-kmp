//
//  CharacterSearchView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 10/17/22.
//

import SwiftUI
import rickandmortysdk

struct CharacterSearchView: View {
    
    let bloc: CharacterSearchBloc
    
    @ObservedObject
    var models: ObservableValue<CharacterSearchBlocModel>
    
    init() {
        let holder = BlocHolder { lifecycle in
            BlocBuilder.shared.createCharacterSearchBloc(lifecycle: lifecycle)
        }
        self.bloc = holder.bloc
        self.models = ObservableValue(holder.bloc.models)
    }
    
    var body: some View {
        let model = models.value
        
        VStack {
            HStack {
                TextField("Character Name", text: Binding(get: { model.query }, set: { query in
                        bloc.onQueryChanged(query: query)
                }))
                        .padding(8)
                        .background(
                                RoundedRectangle(cornerRadius: 4)
                                        .stroke(Color.gray)
                        )
                        .disableAutocorrection(false)
                        .autocapitalization(.none)
                        .submitLabel(.search)
                        .onSubmit {
                            bloc.onSearchClicked()
                        }
                Button(action: {
                    withAnimation {
                        bloc.onSearchClicked()
                    }
                }) {
                    Image(systemName: "magnifyingglass.circle")
                }
            }
            
            Spacer()

        }.navigationBarTitle("Character Search", displayMode: .inline)
            
    }
}
