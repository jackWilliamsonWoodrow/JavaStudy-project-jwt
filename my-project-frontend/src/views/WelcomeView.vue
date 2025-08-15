<script setup>

</script>

<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <div style="flex: 1;background-color: black">
      <el-image style="width: 100%;height: 100%" fit="cover" src="/11.png"/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到我们滴学习平台</div>
      <div style="margin-top: 10px">只能说lbwnb，欢迎你来到这里学习如何使用java，并且与java之父密切交流</div>
    </div>
    <div class="right-card">
      <router-view v-slot="{ Component}">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component"></component>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<style scoped>
.welcome-title{
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: whitesmoke;
  text-shadow: 0 0 10px black;
}
.right-card{
  width: 400px;
  z-index: 1;
  background-color: whitesmoke;
}

</style>