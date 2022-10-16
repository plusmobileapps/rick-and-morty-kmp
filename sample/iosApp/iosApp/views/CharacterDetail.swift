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
            Text(model.character.name)
            Text(model.character.species)
            Text(model.character.status)
            NavigationLink(value: Route.characterSearch) {
                Text("Search")
            }
        }.navigationBarTitle(title, displayMode: .inline)
    }
}
