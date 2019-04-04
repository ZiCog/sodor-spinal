let sizeInBytes = 64 * 1024

for (let address = 0;  address < sizeInBytes; address += 4) {
  data = address
  let addressHex = address.toString(16).padStart(8, '0').toUpperCase()
  let dataHex = data.toString(16).padStart(8, '0').toUpperCase()
  process.stdout.write(`${dataHex}\n`)
}
