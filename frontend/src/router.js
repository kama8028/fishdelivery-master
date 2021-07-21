
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import OrderManager from "./components/OrderManager"

import MyPage from "./components/MyPage"
import PaymentManager from "./components/PaymentManager"

import FishstoreManager from "./components/FishstoreManager"

import DeliveryManager from "./components/DeliveryManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/orders',
                name: 'OrderManager',
                component: OrderManager
            },

            {
                path: '/myPages',
                name: 'MyPage',
                component: MyPage
            },
            {
                path: '/payments',
                name: 'PaymentManager',
                component: PaymentManager
            },

            {
                path: '/fishstores',
                name: 'FishstoreManager',
                component: FishstoreManager
            },

            {
                path: '/deliveries',
                name: 'DeliveryManager',
                component: DeliveryManager
            },



    ]
})
