//
// Created by Andrew Steinmetz on 10/15/22.
//

import SwiftUI
import rickandmortysdk

struct CharacterDetailView: View {
    let blocHolder: BlocHolder<CharacterDetailBloc>

    @ObservedObject
    private var models: ObservableValue<CharacterDetailBlocModel>

    init(id: Int32) {
        let holder = BlocHolder<CharacterDetailBloc> { lifecycle in
            BlocBuilder.shared.createCharacterDetailBloc(lifecycle: lifecycle, id: id)
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
        }
    }
}