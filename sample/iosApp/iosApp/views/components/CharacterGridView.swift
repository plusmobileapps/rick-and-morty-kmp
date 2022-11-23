//
//  CharacterGridView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 10/16/22.
//

import SwiftUI
import rickandmortysdk

struct CharacterGridView: View {
    let characters: [RickAndMortyCharacter]
    
    private let adaptiveColumns = [
        GridItem(.adaptive(minimum: 150))
    ]
    
    var body: some View {
            LazyVGrid(columns: adaptiveColumns) {
                ForEach(characters, id: \.self) { character in
                    NavigationLink(value: Route.characterDetail(character)) {
                        VStack {
                            AsyncImage(url: URL(string: character.imageUrl)) { image in
                                image.resizable()
                            } placeholder: {
                                ProgressView()
                            }.frame(width: 150, height: 150)
                            Text(character.name)
                        }.frame(width: 150)
                    }
                }
            }
    }
}
