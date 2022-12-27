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
        if model.firstPageIsLoading {
            ProgressView()
        } else if model.pageLoadedError != nil && model.pageLoadedError!.isFirstPage {
            Text("Error loading the first page")
        } else {
            VStack {
                List {
                    ForEach(model.episodes) { episode in
                        NavigationLink(value: Route.epidodeDetail(episode)) {
                            HStack {
                                Text(episode.name)
                                Spacer()
                                Text(episode.episode)
                            }
                        }
                    }
                    
                    if model.nextPageIsLoading {
                        ProgressView()
                    }
                    
                    if model.hasMoreToLoad {
                        Button(action: { bloc.loadMore() }) {
                            Text("Load more")
                        }
                    }
                    
                    if model.pageLoadedError != nil {
                        VStack {
                            Text("Error loading next page")
                            Button(action: { bloc.loadMore() }) {
                                Text("Try again")
                            }
                        }
                    }
                }
            }
        }

    }
}

extension Rick_and_morty_apiEpisode: Identifiable {
}
