<template>

<v-data-table
    :headers="headers"
    :items="myPage"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'MyPage',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "orderId", value: "orderId" },
            { text: "customerName", value: "customerName" },
            { text: "fishName", value: "fishName" },
            { text: "qty", value: "qty" },
            { text: "telephone", value: "telephone" },
            { text: "address", value: "address" },
            { text: "status", value: "status" },
        ],
        myPage : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/mypages')

      this.myPage = temp.data._embedded.mypages;

    },
    methods: {
    }
  }
</script>

