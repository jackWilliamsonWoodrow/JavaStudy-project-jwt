<script setup>
import {get} from "@/net/index.js";
import {useRoute} from "vue-router";

const route = useRoute()

const tid = route.params.tid

get(`api/forum/topic?tid=${tid}`,data =>{
  console.info(data)
})
</script>

<template>
1
</template>

<style scoped>

</style>