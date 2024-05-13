import { BottomTabScreenProps, createBottomTabNavigator } from "@react-navigation/bottom-tabs"
import { CompositeScreenProps } from "@react-navigation/native"
import React from "react"
import { ViewStyle } from "react-native"
import { useSafeAreaInsets } from "react-native-safe-area-context"
import { Icon } from "../components"
import { CreateTicketScreen, HomeScreen, SearchScreen } from "../screens"
import { colors, spacing } from "../theme"
import { AppStackParamList, AppStackScreenProps } from "./AppNavigator"
import { ProfileNavigator } from "./ProfileNavigator"
import { TicketNavigator } from "./TicketNavigator"

export type MainTabParamList = {
  Home: undefined
  Search: undefined
  CreateTicket: undefined
  Ticket: undefined
  ProfileNavigator: undefined
}

export type MainTabScreenProps<T extends keyof MainTabParamList> = CompositeScreenProps<
  BottomTabScreenProps<MainTabParamList, T>,
  AppStackScreenProps<keyof AppStackParamList>
>

export const Tab = createBottomTabNavigator<MainTabParamList>()

export function MainNavigator() {
  const { bottom } = useSafeAreaInsets()

  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarShowLabel: false,
        tabBarHideOnKeyboard: true,
        tabBarStyle: [$tabBar, { height: bottom + 56 }],
        tabBarItemStyle: $tabBarItem,
      }}
    >
      <Tab.Screen
        name="Home"
        component={HomeScreen}
        options={{
          tabBarIcon: ({ focused }) => (
            <Icon icon="home" color={focused ? colors.black : undefined} size={24} />
          ),
        }}
      />
      <Tab.Screen
        name="Search"
        component={SearchScreen}
        options={{
          tabBarIcon: ({ focused }) => (
            <Icon icon="search" color={focused ? colors.black : undefined} size={24} />
          ),
        }}
      />
      <Tab.Screen
        name="CreateTicket"
        component={CreateTicketScreen}
        options={{
          tabBarStyle: { display: "none" },
          tabBarIcon: ({ focused }) => (
            <Icon icon="squarePlus" color={focused ? colors.black : undefined} size={24} />
          ),
        }}
      />
      <Tab.Screen
        name="Ticket"
        component={TicketNavigator}
        options={{
          tabBarIcon: ({ focused }) => (
            <Icon icon="ticket" color={focused ? colors.black : undefined} size={24} />
          ),
        }}
      />
      <Tab.Screen
        name="ProfileNavigator"
        component={ProfileNavigator}
        options={{
          tabBarIcon: ({ focused }) => (
            <Icon icon="person" color={focused ? colors.black : undefined} size={24} />
          ),
        }}
      />
    </Tab.Navigator>
  )
}

const $tabBar: ViewStyle = {
  backgroundColor: colors.white,
  borderTopColor: colors.transparent,
}

const $tabBarItem: ViewStyle = {
  paddingTop: spacing.md,
}
