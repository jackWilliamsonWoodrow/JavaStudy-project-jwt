<script setup>

</script>

<template>
  <div>
    <div style="display: flex;justify-content: space-between;margin: 10px 20px">
      <div style="font-size: 45px;color: grey">
        <i class="qi-100"></i>
      </div>
      <div style="font-weight: bold;text-align: center">
        <div style="font-size: 25px">20℃</div>
        <div style="font-size: 15px">多云</div>
      </div>
      <div style="margin: 13px">
        <div style="font-size: 15px">中国 河北省</div>
        <div style="font-size: 14px">邯郸市 永年区</div>
      </div>
    </div>
    <el-divider style="margin: 10px 0"/>
    <div style="display: grid;grid-template-columns: repeat(5,1fr);text-align: center">
      <div v-for="item in 5">
        <div style="font-size: 13px">{{item}}时</div>
        <div style="font-size: 20px">
          <i class="qi-100"></i>
        </div>
        <div style="font-size: 15px">20℃</div>
      </div>
    </div>
  </div>

</template>

<style scoped>

</style>