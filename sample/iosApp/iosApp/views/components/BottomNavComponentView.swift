//
//  BottomNavComponentView.swift
//  iosApp
//
//  Created by Andrew Steinmetz on 9/11/22.
//

import SwiftUI
import rickandmortysdk

struct BottomNavComponentView: View {
    @Environment(\.colorScheme) var currentMode
    
    var navItems: [BottomNavBlocNavItem]
    var onItemClicked: (BottomNavBlocNavItem) -> Void
    
    var body: some View {
        ZStack {
            VisualEffectView(effect: UIBlurEffect(style: .systemThinMaterial))
                .edgesIgnoringSafeArea(.bottom)
                .frame(maxWidth: .infinity, maxHeight: 80) // todo invalid frame dimension
            HStack {
                Spacer()
                ForEach(navItems, id: \.type.id) { item  in
                    Button(action: {
                        withAnimation {
                            onItemClicked(item)
                        }
                    }) {
                        VStack {
                            Image(systemName: getIconName(icon: item.type))
                                .font(.title)
                            Text("Todo")
                        }
                        .foregroundColor(item.selected ? .blue : .gray)
                    }
                    Spacer()
                }
            }.padding(.top, 8)
        }
            
    }
    
    // https://developer.apple.com/sf-symbols/
    private func getIconName(icon: BottomNavBlocNavItem.Type_) -> String {
        switch icon {
        case .characters:
            return "person.3"
        case .episodes:
            return "list.triangle"
        default:
            return "gear"
        }
    }
}
