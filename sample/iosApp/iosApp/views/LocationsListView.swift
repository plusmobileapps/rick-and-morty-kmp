//
// Created by Andrew Steinmetz on 10/15/22.
//

import SwiftUI
import rickandmortysdk

struct LocationsListView: View {
    private let bloc: LocationBloc

    @ObservedObject
    private var models: ObservableValue<LocationBlocModel>

    init(_ bloc: LocationBloc) {
        self.bloc = bloc
        models = ObservableValue(bloc.models)
    }

    var body: some View {
        let model = models.value

        VStack {
            List {
                ForEach(model.locations) { item in
                    NavigationLink(value: Route.locationDetail(item.id)) {
                        HStack {
                            Text(item.name)
                            Spacer()
                            Text("\(item.residents.count) residents")
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

extension Rick_and_morty_apiLocation: Identifiable {
}
