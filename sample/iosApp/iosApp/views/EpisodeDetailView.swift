//
// Created by Andrew Steinmetz on 10/16/22.
//

import SwiftUI
import rickandmortysdk

struct EpisodeDetailView : View {
    let title: String
    let blocHolder: BlocHolder<EpisodeDetailBloc>

    @ObservedObject
    private var models: ObservableValue<EpisodeDetailBlocModel>

    init(title: String, id: Int32) {
        self.title = title
        self.blocHolder = BlocHolder { lifecycle in
            BlocBuilder.shared.createEpisodeDetailBloc(lifecycle: lifecycle, id: id)
        }
        self.models = ObservableValue(blocHolder.bloc.models)
    }

    var body: some View {
        let model = models.value
        
        ScrollView {
            VStack {
                Spacer().frame(height: 16.0)
                Text(model.episode.name)
                        .font(.system(size: 20, weight: .bold, design: .default))
                Spacer().frame(height: 16)
                Text("Aired on: \(model.episode.air_date)")
                        .font(.system(size: 16, weight: .regular, design: .default))
                Spacer().frame(height: 16)
                
                Text("Characters:")
                    .font(.system(size: 18, weight: .bold, design: .default))
                
                CharacterGridView(characters: model.characters)
                
                Spacer()
                
            }
        }.navigationBarTitle(title)
                .toolbar {
                    NavigationLink(value: Route.episodeSearch) {
                        Image(systemName: "magnifyingglass")
                    }
                }
    }
}
