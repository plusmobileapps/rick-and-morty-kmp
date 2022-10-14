//
//  ContentView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import SwiftUI
import rickandmortysdk

struct ContentView: View {
    
    var di: DI
    
    var body: some View {
        TabView {
            Text("1")
                .tabItem {
                    Image(systemName: "person.3")
                    Text("Characters")
                }
            
            Text("2")
                .tabItem {
                    Image(systemName: "list.triangle")
                    Text("Episodes")
                }
        }
        
//        NavigationStack(path: <#T##Binding<NavigationPath>#>, root: <#T##() -> _#>)
    }
}

//struct ContentView_Previews: PreviewProvider {
//    static var previews: some View {
//        ContentView()
//    }
//}
