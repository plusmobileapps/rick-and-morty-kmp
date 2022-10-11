//
//  CharacterDetailView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 10/2/22.
//

import SwiftUI
import rickandmortysdk

struct CharacterDetailView: View {
    
    private let bloc: CharacterDetailBloc
    
    @ObservedObject
    private var models: ObservableValue<CharacterDetailBlocModel>
    
    init(_ bloc: CharacterDetailBloc) {
        self.bloc = bloc
        self.models = ObservableValue(bloc.models)
    }
    
    var body: some View {
        let model = models.value
        NavigationView {
            ScrollView {
                VStack {
                    Text("Hello, World!")
                    Spacer()
                }
            }.navigationBarTitle("Some title", displayMode: .inline)
                        .navigationBarBackButtonHidden(true)
                        .navigationBarItems(leading: Button(action: {
                            withAnimation {
                                bloc.onBackClicked()
                            }
                        }) {
                            Image(systemName: "arrow.left")
                        })
        }.navigationViewStyle(StackNavigationViewStyle())
    }
}
