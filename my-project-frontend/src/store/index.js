import {defineStore} from "pinia"

export const userStore = defineStore('general',{
    state:() =>{
        return {
            user: {
                username: '',
                email:　'',
                role: '',
                registerTime: null
            }
        }
}
})