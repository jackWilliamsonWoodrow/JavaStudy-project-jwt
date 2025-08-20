<script setup>

import Card from "@/components/Card.vue";
import {Message, Refresh, Select, User} from "@element-plus/icons-vue";
import {userStore} from "@/store/index.js";
import {accessHeader, get, post} from "@/net/index.js";
import {ElMessage} from "element-plus";
import {ref} from "vue";
import axios from "axios";


const isEmailValid = ref(true)
const desc = ref('')
const store = userStore()
const coldTime = ref(0)
const baseFormRef = ref()
const emailFormRef = ref()
const registerTime = computed(() => new Date(store.user.registerTime).toLocaleString())
const baseForm = reactive({
  username: '',
  gender: 1,
  phone: '',
  qq: '',
  wx: '',
  desc: ''
})

const emailForm　= reactive({
  email: '',
  code: ''
})


const validateUsername = (rule,value,callback) => {
  if (value === '') {
    callback(new Error('请输入用户名'))
  }else if (!/^[\u4e00-\u9fa5a-zA-Z0-9_]+$/.test(value)){
    callback(new Error('用户名不能包含特殊字符，只能包含中英文和数字'))
  }else {
    callback()
  }
}

const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}


const rules = {
  username: [
    { validator: validateUsername,trigger: ['blur','change']},
    { min: 3,max: 16,message: '用户名长度必须在2-16为之间',trigger: ['blur','change']},
  ],
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur'},
    {
      type: 'email',
      message: 'Please input correct email address',
      trigger: ['blur', 'change'],
    },
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur'},
  ]
}

function saveDetails(){
  baseFormRef.value.validate( isValid => {
    if (isValid){

      post('/api/user/save-details',baseForm,() =>{
        ElMessage.success('用户信息保存成功')
        store.user.username = baseForm.username
        desc.value = baseForm.desc
        loading.base = false
      },(message) =>{
        ElMessage.warning(message)
        loading.base = false
      })

    }
  })
}
get('/api/user/details', data =>{
  baseForm.username = store.user.username
  baseForm.gender = data.gender
  baseForm.phone = data.phone
  baseForm.qq = data.qq
  baseForm.wx = data.wx
  baseForm.desc = data.value = data.desc
  emailForm.email = store.user.email
  loading.form =false
})

const loading = reactive({
  form: true,
  base: false
})

function sendEmailCode(){
  emailFormRef.value.validate(isValid =>{
    if (isValid){
      coldTime.value = 60
      get(`/api/auth/ask-code?email=${emailForm.email}&type=modify`,()=>{
        ElMessage.success(`验证码已成功发送：${emailForm.email},请注意查收`)
        const handle = setInterval(()=>{
          coldTime.value--
          if (coldTime.value === 0){
            clearInterval(handle)
          }
        },1000)
      },(message) =>{
        ElMessage.warning(message)
        coldTime.value = 0
      })
    }
  })
}

function modifyEmail(){
  emailFormRef.value.validate(isValid =>{
    if (isValid){
      post('/api/user/modify-email',emailForm,()=>{
        ElMessage.success('邮件修改成功')
        store.user.email = emailForm.email
        emailForm.code = ''
      })
    }
  })
}

function beforeAvatarUpload(rawFile){
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png'){
    ElMessage.error('头像只能是jpeg或png格式')
    return false
  }else if (rawFile.size /1024 /1024 > 5){
    ElMessage.error('头像大小不能大于5MB')
    return false
  }
  return true
}

function uploadSuccess(response){
  ElMessage.success('头像上传成功')
  store.user.avatar = response.data
}
</script>

<template>
  <div  style="display: flex;max-width: 950px;margin: auto">
    <div class="setting-let">
      <card :icon="User" title="账号信息设置" desc="在这里编辑您的个人信息，您可以在隐私设置中选择是否展示这些信息" v-loading="loading.form">
        <el-form :model="baseForm" ref="baseFormRef" :rules="rules" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="用户名" prop="username" maxlength="16">
            <el-input v-model="baseForm.username"/>
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="baseForm.gender">
              <el-radio label="0">男</el-radio>
              <el-radio label="1">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="手机号" prop="phone" maxlength="11">
            <el-input v-model="baseForm.phone"/>
          </el-form-item>
          <el-form-item label="qq号" prop="qq" maxlength="13">
            <el-input v-model="baseForm.qq"/>
          </el-form-item>
          <el-form-item label="微信号" prop="wx" maxlength="20">
            <el-input v-model="baseForm.wx"/>
          </el-form-item>
          <el-form-item label="个人简介" prop="desc" maxlength="200">
            <el-input v-model="baseForm.desc" type="textarea" :rows="6"/>
          </el-form-item>
          <div>
            <el-button @click="saveDetails" :icon="Select" type="success" :loading="loading.base">保存用户信息</el-button>
          </div>
        </el-form>
      </card>
      <card style="margin-top: 10px" :icon="Message" title="电子邮件地址" desc="您可以在这里修改默认绑定的电子邮件地址">
        <el-form @validate="onValidate" :rules="rules" :model="emailForm" ref="emailFormRef" label-position="top" style="margin: 0 10px 10px 10px">
          <el-form-item label="电子邮件" prop="email">
            <el-input v-model="emailForm.email"/>
          </el-form-item>
          <el-form-item  label="获取验证码" prop="code">
            <el-row style="width: 100%" :gutter="10">
              <el-col :span="18">
                <el-input v-model="emailForm.code" placeholder="请输入验证码"/>
              </el-col>
              <el-col :span="6">
                <el-button type="success" @click="sendEmailCode" :disabled="!isEmailValid || coldTime >0">
                  {{coldTime > 0 ? '请稍候'+ coldTime+ '秒' : "获取验证码"}}
                </el-button>
              </el-col>
            </el-row>
          </el-form-item>
          <div>
            <el-button @click="modifyEmail" :icon="Refresh" type="success">更新电子邮件</el-button>
          </div>
        </el-form>
      </card>
    </div>
    <div class="setting-right">
      <div style="position: sticky; top: 20px">
        <card :show-header="false">
          <div style="text-align: center;padding: 5px 15px 0 15px">
            <el-avatar
                :src="store.avatarUrl"
            />
            <div style="margin: 5px 0">
              <el-upload
                  :action="axios.defaults.baseURL+'/api/image/avatar'"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :on-success="uploadSuccess"
                  :headers="accessHeader()"
              >
                <el-button size="small">修改头像</el-button>
              </el-upload>
            </div>
            <div style="font-weight: bold ">你好，{{store.user.username}}</div>
          </div>
          <el-divider style="margin: 10px"></el-divider>
          <div style="font-size: 14px;color: gray;padding: 10px">
            {{ desc || "这个用户很懒，没有填写个人简介"}}
          </div>
        </card>
        <card :show-header="false" style="margin-top: 10px;font-size: 14px">
          <div>账号注册时间：{{ registerTime }}</div>
          <div style="color: grey">欢迎加入我们的学习论坛</div>
        </card>
      </div>
    </div>
  </div>
</template>

<style scoped>
.setting-let{
  flex: 1;
  margin: 20px;
}
.setting-right {
  width: 300px;
  margin: 20px 30px 20px 0;
}
</style>