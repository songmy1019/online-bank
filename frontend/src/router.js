
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import RequestManager from "./components/RequestManager"

import AuthManager from "./components/AuthManager"

import AccountManager from "./components/AccountManager"

import MessageManager from "./components/MessageManager"

import LoanManager from "./components/LoanManager"

import HistoryManager from "./components/HistoryManager"

export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/requests',
                name: 'RequestManager',
                component: RequestManager
            },

            {
                path: '/auths',
                name: 'AuthManager',
                component: AuthManager
            },

            {
                path: '/accounts',
                name: 'AccountManager',
                component: AccountManager
            },

            {
                path: '/messages',
                name: 'MessageManager',
                component: MessageManager
            },

            {
                path: '/loans',
                name: 'LoanManager',
                component: LoanManager
            },

            {
                path: '/histories',
                name: 'HistoryManager',
                component: HistoryManager
            },



    ]
})
