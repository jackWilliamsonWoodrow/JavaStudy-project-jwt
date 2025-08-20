<script setup>
import {logout,get} from "@/net/index.js";
import router from "@/router/index.js";
import {userStore} from "@/store/index.js";
import {ref} from "vue";
import {
  Location,
  ChatDotSquare,
  Bell,
  Umbrella,
  Notification,
  School,
  Position,
  Document,
  Files,
  Monitor,
  Collection,
  DataLine,
  Operation, User, Search, Message, Back
} from "@element-plus/icons-vue";

const searchInput = reactive({
  type: '1',
  text: ''
})

const store = userStore()
const loading = ref(true)

get('/api/user/info',(data) => {
  store.user = data
  loading.value = false
})
function userLogout() {
  logout(() => router.push('/'))
}

</script>

<template>
  <div class="main-content" v-loading="loading" element-loading-text="正在加载，请稍等...">
    <el-container style="height: 100%">
      <el-header class="main-content-header" v-if="!loading">
        <el-image class="logo" src="/battlefield.png"></el-image>
        <div style="flex: 1;padding: 0 20px;text-align: center">
          <el-input v-model="searchInput.text" style="width: 100%;max-width: 500px" placeholder="搜索论坛相关内容...">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-select v-model="searchInput.type" style="width: 120px">
                <el-option value="1" label="帖子广场"></el-option>
                <el-option value="2" label="校园活动"></el-option>
                <el-option value="3" label="表白墙"></el-option>
                <el-option value="4" label="教务通知"></el-option>
              </el-select>
            </template>
          </el-input>
        </div>
        <div style="flex: 1" class="user-info">
          <div class="profile">
            <div>{{ store.user.username }}</div>
            <div>{{ store.user.email }}</div>
          </div>
          <el-dropdown>
            <el-avatar
                :src="store.avatarUrl"
            />
            <template #dropdown>
              <el-dropdown-item>
                <el-icon><Operation /></el-icon>
                个人设置
              </el-dropdown-item>
              <el-dropdown-item>
                <el-icon><Message /></el-icon>
                消息列表
              </el-dropdown-item>
              <el-dropdown-item @click="userLogout" divided>
                <el-icon><Back /></el-icon>
                退出登录
              </el-dropdown-item>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-container>
        <el-aside width="230px">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <el-menu router
                :default-active="$route.path"
                :default-openeds="['1','2','3']"
                     style="height: calc(100vh - 55px)">
              <el-sub-menu index="1">
                <template #title>
                  <el-icon><Location/></el-icon>
                  <span><b>校园论坛</b></span>
                </template>
                <el-menu-item index="/index">
                  <template #title>
                    <el-icon><ChatDotSquare/></el-icon>
                    帖子广场
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Bell/></el-icon>
                    失物招领
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Notification/></el-icon>
                    校园活动
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Umbrella/></el-icon>
                    表白墙
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><School/></el-icon>
                    小王埃上淑
                    <el-tag style="margin-left: 5px" size="small">合作机构</el-tag>
                  </template>
                </el-menu-item>
              </el-sub-menu>
              <el-sub-menu index="2">
                <template #title>
                  <el-icon><Position /></el-icon>
                  <span><b>探索与发现</b></span>
                </template>
                <el-menu-item>
                  <template #title>
                    <el-icon><Document /></el-icon>
                    <span><b>成绩查询</b></span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Files /></el-icon>
                    <span><b>班级课程表</b></span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Monitor /></el-icon>
                    <span><b>教务通知</b></span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><Collection /></el-icon>
                    <span><b>在线图书馆</b></span>
                  </template>
                </el-menu-item>
                <el-menu-item>
                  <template #title>
                    <el-icon><DataLine /></el-icon>
                    <span><b>预约教室</b></span>
                  </template>
                </el-menu-item>
              </el-sub-menu  >
              <el-sub-menu index="3">
                <template #title>
                  <el-icon><Operation /></el-icon>
                  <span><b>设置</b></span>
                </template>
                <el-menu-item index="/index/user-setting">
                  <template #title>
                    <el-icon><User /></el-icon>
                    <span><b>个人资料设置</b></span>
                  </template>
                </el-menu-item>
                <el-menu-item index="/index/privacy-setting">
                  <template #title>
                    <el-icon><Operation /></el-icon>
                    <span><b>账号安全设置</b></span>
                  </template>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </el-scrollbar>
        </el-aside>
        <el-main class="main-content-page">
          <el-scrollbar style="height: calc(100vh - 55px)">
            <router-view v-slot="{ Component }">
              <transition name="el-fade-in-liner" mode="out-in">
                <component :is="Component" style="height: 100%"></component>
              </transition>
            </router-view>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style lang="less" scoped>
.main-content{
  height: 100vh;
  width: 100vw;
}
.main-content-page{
  padding: 0;
  background-color: whitesmoke;
}
.dark .main-content-page{
  background-color: #212225;
}
.main-content-header {
  border-bottom: solid 1px var(--el-border-color);
  height: 55px;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  .logo{
    height: 32px;
  }

  .user-info{
    display: flex;
    justify-content: flex-end;
    align-items: center;

    .el-avatar:hover {
      cursor: pointer;
    }

    .profile{
      text-align: right;
      margin-right: 20px;

      :first-child{
        font-size: 18px;
        font-weight: bold;
        line-height: 20px;
      }

      :last-child{
        font-size: 10px;
        color: grey;
      }
    }
  }
}

</style>