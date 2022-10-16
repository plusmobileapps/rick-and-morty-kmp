//
// Created by Andrew Steinmetz on 10/15/22.
//

import SwiftUI
import rickandmortysdk

struct CharacterDetailView: View {
    let title: String
    let blocHolder: BlocHolder<CharacterDetailBloc>

    @ObservedObject
    private var models: ObservableValue<CharacterDetailBlocModel>

    init(_ character: RickAndMortyCharacter) {
        title = character.name
        let holder = BlocHolder<CharacterDetailBloc> { lifecycle in
            BlocBuilder.shared.createCharacterDetailBloc(lifecycle: lifecycle, id: character.id)
        }
        self.blocHolder = holder
        self.models = ObservableValue(holder.bloc.models)
    }

    var body: some View {
        let model = models.value

        VStack {
            Spacer().frame(height: 16.0)
            AsyncImage(url: URL(string: model.character.imageUrl)) { image in
                image.resizable()
            } placeholder: {
                ProgressView()
            }.frame(width: 300, height: 300)
            Text(model.character.name)
            Text(model.character.species)
            Text(model.character.status)
            NavigationLink(value: Route.characterSearch) {
                Text("Search")
            }
            Spacer()
        }.navigationBarTitle(title, displayMode: .inline)
    }
}
