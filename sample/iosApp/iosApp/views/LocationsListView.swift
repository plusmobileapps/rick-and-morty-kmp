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
                    switch item {
                    case let locationItem as LocationListItem.LocationItem:
                        NavigationLink(value: Route.locationDetail(locationItem.value.id)) {
                            HStack {
                                Text(locationItem.value.name)
                                Spacer()
                                Text("\(locationItem.value.residents.count) residents")
                            }
                        }
                    case _ as LocationListItem.NextPageLoading:
                        ProgressView()
                    default: EmptyView()
                    }
                }
            }
        }
    }
}

extension LocationListItem: Identifiable {
}