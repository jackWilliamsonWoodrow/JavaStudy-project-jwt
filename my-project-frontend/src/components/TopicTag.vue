<script setup>
import {userStore} from "@/store/index.js";

const store = userStore()
defineProps({
  type: Number
})
</script>

<template>
  <div class="topic-type"
       :style="{
                      color: store.findTypeById(type)?.color +'EE',
                      'border-color': store.findTypeById(type)?.color +'DD',
                      'background-color': store.findTypeById(type)?.color + '33'
                   }">
    {{store.findTypeById(type)?.name}}
  </div>
</template>

<style scoped>
.topic-type{
  display: inline-block;
  border: solid 1px grey;
  border-radius: 5px;
  font-size: 12px;
  padding: 0 5px;
}
</style>