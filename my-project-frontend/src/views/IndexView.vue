<script setup>
import {logout} from "@/net/index.js";
import router from "@/router/index.js";

function userLogout() {
  logout(() => router.push('/'))
}

</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-header>Header</el-header>
      <el-container>
        <el-aside width="200px">Aside</el-aside>
        <el-main>Main</el-main>
      </el-container>
    </el-container>
  </div>

</template>

<style scoped>

</style>