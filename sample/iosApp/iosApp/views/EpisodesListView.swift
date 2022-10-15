//
//  EpisodesListView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 10/14/22.
//

import SwiftUI
import rickandmortysdk

struct EpisodesListView: View {

    private let bloc: EpisodesBloc

    @ObservedObject
    private var models: ObservableValue<EpisodesBlocModel>

    init(_ bloc: EpisodesBloc) {
        self.bloc = bloc
        models = ObservableValue(bloc.models)
    }

    var body: some View {
        let model = models.value
        VStack {
            List {
                ForEach(model.episodes) { item in
                    switch item {
                    case let episodeItem as EpisodeListItem.EpisodeItem:
                        NavigationLink(value: Route.epidodeDetail(episodeItem.value.id)) {
                            HStack {
                                Text(episodeItem.value.name)
                                Spacer()
                                Text(episodeItem.value.episode)
                            }
                        }
                    case _ as EpisodeListItem.NextPageLoading:
                        ProgressView()
                    default: EmptyView()
                    }
                }
            }
        }
    }
}

extension EpisodeListItem: Identifiable {
}

class EpisodesBlocHolder : ObservableObject {
    let lifecycle: LifecycleRegistry
    let bloc: EpisodesBloc

    init() {
        lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        lifecycle.onCreate()
        bloc = BlocBuilder.shared.createEpisodesBloc(lifecycle: lifecycle)
    }

    func setOutputListener(_ output: @escaping (EpisodesBlocOutput) -> Void) {
        BlocBuilder.shared.episodesBlocOutput = output
    }

    deinit {
        lifecycle.onDestroy()
    }
}

//struct EpisodesListView_Previews: PreviewProvider {

//    static var previews: some View {
//        EpisodesListView()
//    }
//}
