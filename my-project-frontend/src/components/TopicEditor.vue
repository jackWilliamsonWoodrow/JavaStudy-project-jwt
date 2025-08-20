<script setup>
import {Document} from "@element-plus/icons-vue";
import {QuillEditor} from "@vueup/vue-quill";
import "@vueup/vue-quill/dist/vue-quill.snow.css"

defineProps({
  show: Boolean
})
const editor = reactive({
  type: null,
  title: '',
  text: ''
})
const emit = defineEmits(['close'])

const types = [
  {id: 1,name: '日常闲聊',desc: '在这里分享你的日常'},
  {id: 2,name: '真诚交友',desc: 'dddddd'},
  {id: 3,name: '问题反馈',desc: '在这里分享eeeeee你的日常'},
  {id: 4,name: '恋爱官宣',desc: '在这eewrwe日常'},
]
</script>

<template>
  <div>
    <el-drawer :model-value="show"
               :direction="'btt'" :size="600"
               :close-on-click-modal="false" @close="emit('close')">
      <template #header>
        <div>
          <div style="font-weight: bold">发表新的帖子</div>
          <div style="font-size: 13px">请大家自觉遵守相关法律法规发言，和谐讨论，感谢！！！</div>
        </div>
      </template>
      <div style="display: flex;gap: 10px">
        <div style="width: 150px">
          <el-select placeholder="请选择主题类型..." v-model="editor.type">
            <el-option v-for="item in types" :value="item.id" :label="item.name"></el-option>
          </el-select>
        </div>
        <div style="flex: 1">
          <el-input style="height: 100%" v-model="editor.title" placeholder="请输入帖子标题" :prefix-icon="Document"/>
        </div>
      </div>
      <div style="margin-top: 10px;height: 410px">
        <quill-editor v-model:content="editor.text" style="height: calc(100% - 45px)"
        placeholder="今天想分享点什么呢？"/>
      </div>
      <div style="display: flex;justify-content: space-between;margin-top: 5px">
        <div style="color: grey;font-size: 13px">
          当前字数666
        </div>
        <div>
          <el-button type="success" plain>立即发布</el-button>
        </div>
      </div>
    </el-drawer>
  </div>

</template>

<style scoped>
:deep(.el-drawer){
  width: 800px;
  margin: auto;
  border-radius: 10px 10px 0 0;
}
:deep(.el-drawer__header){
  margin: 0;
}
:deep(.ql-toolbar){
  border-radius: 5px 5px 0 0;
  border-color: var(--el-border-color);
}
:deep(.ql-container){
  border-radius: 0 0 5px 5px;
  border-color: var(--el-border-color);
}
:deep(.ql-editor){
  font-size: 14px;
}
:deep(.ql-editor.ql-blank::before){
  color: var(--el-text-color-placeholder);
  font-style: normal;
}

</style>