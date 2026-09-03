// 菜品数据：以后可以替换成从后端接口获取
const menu = [
  { id: 1, name: "鸡胸肉三明治", category: "主食", price: 22, desc: "全麦面包搭配蔬菜和嫩煎鸡胸", tag: "招牌" },
  { id: 2, name: "牛油果吐司", category: "主食", price: 26, desc: "厚切吐司配牛油果与溏心蛋", tag: "人气" },
  { id: 3, name: "日式咖喱饭", category: "主食", price: 28, desc: "温润咖喱配米饭与时蔬", tag: "" },
  { id: 4, name: "香脆薯角", category: "小吃", price: 15, desc: "外脆里嫩，附番茄蘸酱", tag: "" },
  { id: 5, name: "柠檬鸡翅", category: "小吃", price: 19, desc: "柠香微甜，两份起点更划算", tag: "推荐" },
  { id: 6, name: "时蔬沙拉", category: "小吃", price: 18, desc: "新鲜生菜、圣女果与油醋汁", tag: "低卡" },
  { id: 7, name: "鲜榨橙汁", category: "饮品", price: 14, desc: "每日鲜果现榨，不加糖", tag: "" },
  { id: 8, name: "冰美式", category: "饮品", price: 12, desc: "中度烘焙，清爽提神", tag: "" },
  { id: 9, name: "燕麦酸奶杯", category: "甜点", price: 16, desc: "希腊酸奶配燕麦与水果", tag: "轻负担" },
  { id: 10, name: "巧克力布朗尼", category: "甜点", price: 18, desc: "浓郁微苦，甜而不腻", tag: "人气" }
];

const EMOJI = {
  主食: "🥪",
  小吃: "🍟",
  饮品: "🥤",
  甜点: "🍰"
};

const menuGrid = document.getElementById("menuGrid");
const cartItemsEl = document.getElementById("cartItems");
const cartCountEl = document.getElementById("cartCount");
const totalPriceEl = document.getElementById("totalPrice");
const searchBox = document.getElementById("searchBox");
const categoryButtons = Array.from(document.querySelectorAll(".category-btn"));

let currentCategory = "全部";
let keyword = "";

// 购物车结构：{ id -> { count } }
let cart = loadCart();

function loadCart() {
  try {
    const saved = JSON.parse(localStorage.getItem("ordering_cart") || "{}");
    return saved && typeof saved === "object" ? saved : {};
  } catch (error) {
    return {};
  }
}

function saveCart() {
  localStorage.setItem("ordering_cart", JSON.stringify(cart));
}

function getFilteredMenu() {
  return menu.filter((item) => {
    const matchCategory = currentCategory === "全部" || item.category === currentCategory;
    const matchKeyword = !keyword || item.name.includes(keyword) || item.desc.includes(keyword);
    return matchCategory && matchKeyword;
  });
}

function renderMenu() {
  const list = getFilteredMenu();
  if (list.length === 0) {
    menuGrid.innerHTML = '<p class="empty-search">没有找到符合条件的菜品，换个关键词试试～</p>';
    return;
  }
  menuGrid.innerHTML = list.map((item) => `
    <article class="dish-card">
      <div class="dish-placeholder" aria-hidden="true">${EMOJI[item.category] || "🍽️"}</div>
      <div class="dish-body">
        <div class="dish-title">
          <span class="dish-name">${item.name}</span>
          <span class="dish-price">¥${item.price}</span>
        </div>
        <p class="dish-desc">${item.desc}</p>
        ${item.tag ? `<span class="dish-tag">${item.tag}</span>` : ""}
        <button class="btn add-btn" data-id="${item.id}">加入购物车</button>
      </div>
    </article>
  `).join("");
}

function addToCart(id) {
  const item = menu.find((it) => it.id === id);
  if (!item) return;
  cart[id] = { count: (cart[id]?.count || 0) + 1 };
  saveCart();
  renderCart();
}

function changeQty(id, delta) {
  if (!cart[id]) return;
  cart[id].count += delta;
  if (cart[id].count <= 0) {
    delete cart[id];
  }
  saveCart();
  renderCart();
}

function renderCart() {
  const ids = Object.keys(cart);
  cartCountEl.textContent = ids.reduce((sum, id) => sum + cart[id].count, 0);

  if (ids.length === 0) {
    cartItemsEl.innerHTML = '<li class="empty-tip">购物车还是空的</li>';
    totalPriceEl.textContent = "¥0";
    return;
  }

  cartItemsEl.innerHTML = ids.map((id) => {
    const dish = menu.find((it) => it.id === Number(id));
    const count = cart[id].count;
    if (!dish) return "";
    return `
      <li class="cart-item">
        <div class="cart-info">
          <div class="cart-name">${dish.name}</div>
          <div class="cart-unit">¥${dish.price} × ${count}</div>
        </div>
        <div class="qty-control">
          <button class="qty-btn" data-action="minus" data-id="${dish.id}">−</button>
          <span>${count}</span>
          <button class="qty-btn" data-action="plus" data-id="${dish.id}">+</button>
        </div>
      </li>
    `;
  }).join("");

  const total = ids.reduce((sum, id) => {
    const dish = menu.find((it) => it.id === Number(id));
    return sum + (dish ? dish.price * cart[id].count : 0);
  }, 0);
  totalPriceEl.textContent = `¥${total}`;
}

categoryButtons.forEach((btn) => {
  btn.addEventListener("click", () => {
    categoryButtons.forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");
    currentCategory = btn.dataset.category;
    renderMenu();
  });
});

searchBox.addEventListener("input", (event) => {
  keyword = event.target.value.trim();
  renderMenu();
});

menuGrid.addEventListener("click", (event) => {
  const btn = event.target.closest(".add-btn");
  if (btn) addToCart(Number(btn.dataset.id));
});

cartItemsEl.addEventListener("click", (event) => {
  const btn = event.target.closest(".qty-btn");
  if (!btn) return;
  const id = Number(btn.dataset.id);
  changeQty(id, btn.dataset.action === "plus" ? 1 : -1);
});

document.getElementById("clearCart").addEventListener("click", () => {
  if (Object.keys(cart).length === 0) return;
  cart = {};
  saveCart();
  renderCart();
});

document.getElementById("checkout").addEventListener("click", () => {
  const total = totalPriceEl.textContent;
  const count = cartCountEl.textContent;
  if (Number(count) === 0) {
    alert("购物车还没有商品哦");
    return;
  }
  alert(`订单已提交（演示）：共 ${count} 件，合计 ${total}\n在真实项目中，这里会调用后端接口保存订单。`);
  cart = {};
  saveCart();
  renderCart();
});

renderMenu();
renderCart();
