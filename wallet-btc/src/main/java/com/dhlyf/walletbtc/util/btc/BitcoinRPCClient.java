package com.dhlyf.walletbtc.util.btc;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import com.dhlyf.walletmodel.base.SpringContextUtil;
import com.dhlyf.walletmodel.common.Chain;
import com.dhlyf.walletmodel.common.CommonResult;
import com.dhlyf.walletmodel.common.WalletException;
import com.dhlyf.walletmodel.common.WalletResult;
import com.dhlyf.walletmodel.web.req.RequestData;
import com.dhlyf.walletmodel.web.req.cool.CoolWalletFormatParam;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class BitcoinRPCClient implements Bitcoin {
    private static final Logger logger = Logger.getLogger(BitcoinRPCClient.class.getCanonicalName());
    public final URL rpcURL;
    private URL noAuthURL;
    private String authStr;
    private String walletPath;
    private HostnameVerifier hostnameVerifier;
    private SSLSocketFactory sslSocketFactory;
    private int connectTimeout;
    public static final Charset QUERY_CHARSET = Charset.forName("UTF-8");

    private String coolWalletUrl;
    public BitcoinRPCClient(String rpcUrl,String walletPath,String coolWalletUrl) throws MalformedURLException {
        this(new URL(rpcUrl), walletPath, coolWalletUrl);
    }

    public BitcoinRPCClient(URL rpc, String walletPath, String coolWalletUrl) {
        this.hostnameVerifier = null;
        this.sslSocketFactory = null;
        this.connectTimeout = 0;
        this.rpcURL = rpc;
        this.walletPath = walletPath;
        this.coolWalletUrl = coolWalletUrl;

        try {
            this.noAuthURL = (new URI(rpc.getProtocol(), (String)null, rpc.getHost(), rpc.getPort(), rpc.getPath(), rpc.getQuery(), (String)null)).toURL();
        } catch (MalformedURLException var3) {
            throw new IllegalArgumentException(rpc.toString(), var3);
        } catch (URISyntaxException var4) {
            throw new IllegalArgumentException(rpc.toString(), var4);
        }

        this.authStr = rpc.getUserInfo() == null ? null : String.valueOf(Base64Coder.encode(rpc.getUserInfo().getBytes(Charset.forName("ISO8859-1"))));
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return this.sslSocketFactory;
    }

    public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout can not be negative");
        } else {
            this.connectTimeout = timeout;
        }
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public byte[] prepareRequest(final String method, final Object... params) {
        return JSON.stringify(new LinkedHashMap() {
            {
                this.put("method", method);
                this.put("params", params);
                this.put("id", "1");
            }
        }).getBytes(QUERY_CHARSET);
    }

    private static byte[] loadStream(InputStream in, boolean close) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];

        while(true) {
            int nr = in.read(buffer);
            if (nr == -1) {
                return o.toByteArray();
            }

            if (nr == 0) {
                throw new IOException("Read timed out");
            }

            o.write(buffer, 0, nr);
        }
    }

    public Object loadResponse(InputStream in, Object expectedID, boolean close) throws IOException, BitcoinException {
        Object var6;
        try {
            String r = new String(loadStream(in, close), QUERY_CHARSET);
            logger.log(Level.FINE, "Bitcoin JSON-RPC response:\n{0}", r);

            try {
                JSONObject response = JSONObject.parseObject(r);
                if (!expectedID.equals(response.get("id"))) {
                    throw new BitcoinRPCException("Wrong response ID (expected: " + String.valueOf(expectedID) + ", response: " + response.get("id") + ")");
                }

                if (response.get("error") != null) {
                    throw new BitcoinException(JSON.stringify(response.get("error")));
                }

                var6 = response.get("result");
            } catch (ClassCastException var10) {
                throw new BitcoinRPCException("Invalid server response format (data: \"" + r + "\")");
            }
        } finally {
            if (close) {
                in.close();
            }

        }

        return var6;
    }

    public Object query(String method, Object... o) throws BitcoinException {
        try {
            URL targetURL = this.walletPath.isEmpty() ? this.noAuthURL : new URL(this.noAuthURL + "/wallet/" + this.walletPath);
            HttpURLConnection conn = (HttpURLConnection) targetURL.openConnection();
            //            HttpURLConnection conn = (HttpURLConnection)this.noAuthURL.openConnection();
            if (this.connectTimeout != 0) {
                conn.setConnectTimeout(this.connectTimeout);
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (conn instanceof HttpsURLConnection) {
                if (this.hostnameVerifier != null) {
                    ((HttpsURLConnection)conn).setHostnameVerifier(this.hostnameVerifier);
                }

                if (this.sslSocketFactory != null) {
                    ((HttpsURLConnection)conn).setSSLSocketFactory(this.sslSocketFactory);
                }
            }

            conn.setRequestProperty("Authorization", "Basic " + this.authStr);
            byte[] r = this.prepareRequest(method, o);
            logger.log(Level.FINE, "Bitcoin JSON-RPC request:\n{0}", new String(r, QUERY_CHARSET));
            conn.getOutputStream().write(r);
            conn.getOutputStream().close();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new BitcoinRPCException("RPC Query Failed (method: " + method + ", params: " + Arrays.deepToString(o) + ", response header: " + responseCode + " " + conn.getResponseMessage() + ", response: " + new String(loadStream(conn.getErrorStream(), true)));
            } else {
                return this.loadResponse(conn.getInputStream(), "1", true);
            }
        } catch (IOException var6) {
            throw new BitcoinRPCException("RPC Query Failed (method: " + method + ", params: " + Arrays.deepToString(o) + ")", var6);
        }
    }

    @Override
    public void addNode(String node, AddNoteCmd command) throws BitcoinException {
        this.query("addnode", node, command.toString());
    }

    @Override
    public String createRawTransaction(List<TxInput> inputs, List<TxOutput> outputs) throws BitcoinException {
        List<Map> pInputs = new ArrayList();
        Iterator var4 = inputs.iterator();

        while(var4.hasNext()) {
            final TxInput txInput = (TxInput)var4.next();
            pInputs.add(new LinkedHashMap() {
                {
                    this.put("txid", txInput.txid());
                    this.put("vout", txInput.vout());
                }
            });
        }

        Map<String, BigDecimal> pOutputs = new LinkedHashMap();
        Iterator var6 = outputs.iterator();

        while(var6.hasNext()) {
            TxOutput txOutput = (TxOutput)var6.next();
            BigDecimal oldValue;
            if ((oldValue = (BigDecimal)pOutputs.put(txOutput.address(), txOutput.amount())) != null) {
                pOutputs.put(txOutput.address(), oldValue.add(txOutput.amount()));
            }
        }

        return (String)this.query("createrawtransaction", pInputs, pOutputs);
    }

    @Override
    public RawTransaction decodeRawTransaction(String hex) throws BitcoinException {
        return new RawTransactionImpl((Map)this.query("decoderawtransaction", hex));
    }

    @Override
    public String dumpPrivKey(String address) throws BitcoinException {
        return (String)this.query("dumpprivkey", address);
    }

    @Override
    public String getAccount(String address) throws BitcoinException {
        return (String)this.query("getaccount", address);
    }

    @Override
    public String getAccountAddress(String account) throws BitcoinException {
        return (String)this.query("getaddressesbylabel", account);
    }

    @Override
    public List<String> getAddressesByAccount(String account) throws BitcoinException {
        return (List)this.query("getaddressesbyaccount", account);
    }

    @Override
    public double getBalance() throws BitcoinException {
        return ((Number)this.query("getbalance")).doubleValue();
    }

    @Override
    public double getBalance(String account) throws BitcoinException {
        return ((Number)this.query("getbalance", account)).doubleValue();
    }

    @Override
    public double getBalance(String account, int minConf) throws BitcoinException {
        return ((Number)this.query("getbalance", account, minConf)).doubleValue();
    }

    @Override
    public Block getBlock(String blockHash) throws BitcoinException {
        return new BlockMapWrapper((Map)this.query("getblock", blockHash));
    }

    @Override
    public int getBlockCount() {
        try{
            return ((Number)this.query("getblockcount")).intValue();
        }catch (Exception e){
            log.error("get block count error",e);
//            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    @Override
    public String getBlockHash(int blockId) throws BitcoinException {
        return (String)this.query("getblockhash", blockId);
    }

    @Override
    public int getConnectionCount() throws BitcoinException {
        return ((Number)this.query("getconnectioncount")).intValue();
    }

    @Override
    public double getDifficulty() throws BitcoinException {
        return ((Number)this.query("getdifficulty")).doubleValue();
    }

    @Override
    public boolean getGenerate() throws BitcoinException {
        return (Boolean)this.query("getgenerate");
    }

    @Override
    public double getHashesPerSec() throws BitcoinException {
        return ((Number)this.query("gethashespersec")).doubleValue();
    }

    @Override
    public Info getInfo() throws BitcoinException {
        return new InfoMapWrapper((Map)this.query("getinfo"));
    }

    @Override
    public MiningInfo getMiningInfo() throws BitcoinException {
        return new MiningInfoMapWrapper((Map)this.query("getmininginfo"));
    }

    @Override
    public String getNewAddress() throws BitcoinException {
        return (String)this.query("getnewaddress");
    }

    @Override
    public String getNewAddress(String account) throws BitcoinException {
        return (String)this.query("getnewaddress", account);
    }

    @Override
    public PeerInfo getPeerInfo() throws BitcoinException {
        return new PeerInfoMapWrapper((Map)this.query("getmininginfo"));
    }

    @Override
    public String getRawTransactionHex(String txId) throws BitcoinException {
        return (String)this.query("getrawtransaction", txId);
    }

    @Override
    public RawTransaction getRawTransaction(String txId) throws BitcoinException {
        return new RawTransactionImpl((Map)this.query("getrawtransaction", txId, 1));
    }

    @Override
    public double getReceivedByAccount(String account) throws BitcoinException {
        return ((Number)this.query("getreceivedbyaccount", account)).doubleValue();
    }

    @Override
    public double getReceivedByAccount(String account, int minConf) throws BitcoinException {
        return ((Number)this.query("getreceivedbyaccount", account, minConf)).doubleValue();
    }

    @Override
    public double getReceivedByAddress(String address) throws BitcoinException {
        return ((Number)this.query("getreceivedbyaddress", address)).doubleValue();
    }

    @Override
    public double getReceivedByAddress(String address, int minConf) throws BitcoinException {
        return ((Number)this.query("getreceivedbyaddress", address, minConf)).doubleValue();
    }

    @Override
    public RawTransaction getTransaction(String txId) throws BitcoinException {
        return new RawTransactionImpl((Map)this.query("gettransaction", txId));
    }

    @Override
    public TxOutSetInfo getTxOutSetInfo() throws BitcoinException {
        final Map txoutsetinfoResult = (Map)this.query("gettxoutsetinfo");
        return new TxOutSetInfo() {

            @Override
            public int height() {
                return ((Number)txoutsetinfoResult.get("height")).intValue();
            }

            @Override
            public String bestBlock() {
                return (String)txoutsetinfoResult.get("bestblock");
            }

            @Override
            public int transactions() {
                return ((Number)txoutsetinfoResult.get("transactions")).intValue();
            }

            @Override
            public int txOuts() {
                return ((Number)txoutsetinfoResult.get("txouts")).intValue();
            }

            @Override
            public int bytesSerialized() {
                return ((Number)txoutsetinfoResult.get("bytes_serialized")).intValue();
            }

            @Override
            public String hashSerialized() {
                return (String)txoutsetinfoResult.get("hash_serialized");
            }

            @Override
            public double totalAmount() {
                return ((Number)txoutsetinfoResult.get("total_amount")).doubleValue();
            }

            @Override
            public String toString() {
                return txoutsetinfoResult.toString();
            }
        };
    }

    @Override
    public Work getWork() throws BitcoinException {
        final Map workResult = (Map)this.query("getwork");
        return new Work() {
            @Override
            public String midstate() {
                return (String)workResult.get("midstate");
            }

            @Override
            public String data() {
                return (String)workResult.get("data");
            }

            @Override
            public String hash1() {
                return (String)workResult.get("hash1");
            }

            @Override
            public String target() {
                return (String)workResult.get("target");
            }

            @Override
            public String toString() {
                return workResult.toString();
            }
        };
    }

    @Override
    public void importPrivKey(String bitcoinPrivKey) throws BitcoinException {
        this.query("importprivkey", bitcoinPrivKey);
    }

    @Override
    public void importPrivKey(String bitcoinPrivKey, String label) throws BitcoinException {
        this.query("importprivkey", bitcoinPrivKey, label);
    }

    @Override
    public void importPrivKey(String bitcoinPrivKey, String label, boolean rescan) throws BitcoinException {
        this.query("importprivkey", bitcoinPrivKey, label, rescan);
    }

    @Override
    public Map<String, Number> listAccounts() throws BitcoinException {
        return (Map)this.query("listaccounts");
    }

    @Override
    public Map<String, Number> listAccounts(int minConf) throws BitcoinException {
        return (Map)this.query("listaccounts", minConf);
    }

    @Override
    public List<ReceivedAddress> listReceivedByAccount() throws BitcoinException {
        return new ReceivedAddressListWrapper((List)this.query("listreceivedbyaccount"));
    }

    @Override
    public List<ReceivedAddress> listReceivedByAccount(int minConf) throws BitcoinException {
        return new ReceivedAddressListWrapper((List)this.query("listreceivedbyaccount", minConf));
    }

    @Override
    public List<ReceivedAddress> listReceivedByAccount(int minConf, boolean includeEmpty) throws BitcoinException {
        return new ReceivedAddressListWrapper((List)this.query("listreceivedbyaccount", minConf, includeEmpty));
    }

    @Override
    public List<ReceivedAddress> listReceivedByAddress() throws BitcoinException {
        return new ReceivedAddressListWrapper((List)this.query("listreceivedbyaddress"));
    }

    @Override
    public List<ReceivedAddress> listReceivedByAddress(int minConf) throws BitcoinException {
        return new ReceivedAddressListWrapper((List)this.query("listreceivedbyaddress", minConf));
    }

    @Override
    public List<ReceivedAddress> listReceivedByAddress(int minConf, boolean includeEmpty) throws BitcoinException {
        return new ReceivedAddressListWrapper((List)this.query("listreceivedbyaddress", minConf, includeEmpty));
    }

    @Override
    public TransactionsSinceBlock listSinceBlock() throws BitcoinException {
        return new TransactionsSinceBlockImpl((Map)this.query("listsinceblock"));
    }

    @Override
    public TransactionsSinceBlock listSinceBlock(String blockHash) throws BitcoinException {
        return new TransactionsSinceBlockImpl((Map)this.query("listsinceblock", blockHash));
    }

    @Override
    public TransactionsSinceBlock listSinceBlock(String blockHash, int targetConfirmations) throws BitcoinException {
        return new TransactionsSinceBlockImpl((Map)this.query("listsinceblock", blockHash, targetConfirmations));
    }

    @Override
    public List<Transaction> listTransactions() throws BitcoinException {
        return new TransactionListMapWrapper((List)this.query("listtransactions"));
    }

    @Override
    public List<Transaction> listTransactions(String account) throws BitcoinException {
        return new TransactionListMapWrapper((List)this.query("listtransactions", account));
    }

    @Override
    public List<Transaction> listTransactions(String account, int count) throws BitcoinException {
        return new TransactionListMapWrapper((List)this.query("listtransactions", account, count));
    }

    @Override
    public List<Transaction> listTransactions(String account, int count, int from) throws BitcoinException {
        return new TransactionListMapWrapper((List)this.query("listtransactions", account, count, from));
    }

    @Override
    public List<Unspent> listUnspent() throws BitcoinException {
        return new UnspentListWrapper((List)this.query("listunspent"));
    }

    @Override
    public List<Unspent> listUnspent(int minConf) throws BitcoinException {
        return new UnspentListWrapper((List)this.query("listunspent", minConf));
    }

    @Override
    public List<Unspent> listUnspent(int minConf, int maxConf) throws BitcoinException {
        return new UnspentListWrapper((List)this.query("listunspent", minConf, maxConf));
    }

    @Override
    public List<Unspent> listUnspent(int minConf, int maxConf, String... addresses) throws BitcoinException {
        return new UnspentListWrapper((List)this.query("listunspent", minConf, maxConf, addresses));
    }

    public List<Unspent> listUnspent(String... addresses) {
        try{
            return listUnspent(0, 9999999, addresses);
        }catch (Exception e){
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

//    public List<Unspent> listUnspentByRemote(String... addresses) {
//        //https://mempool.space/api/address/1D26eyDp73hR89ese6DqNv73sycWLEoN62/utxo
//        String body = HttpUtil.createPost("https://mempool.space/api/address/"+addresses[0]+"/utxo")
//                .execute().body();
//        JSONObject jsonObject = JSONObject.parseObject(body);
//        UnSpentUtxo utxo = new UnSpentUtxo();
//        utxo.setHash(jsonObject.getString("txid"));
//        utxo.setTxN(jsonObject.getLong("vout"));
//        utxo.setValue(jsonObject.getLong("value"));
//        utxo.setHeight(jsonObject.getInteger("block_height"));
//        utxo.setScript(jsonObject.get("scriptPubKey").get("hex"));
//        utxo.setAddress(jsonObject.get("scriptPubKey").get("address"));
//    }

    @Override
    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount) throws BitcoinException {
        return (String)this.query("sendfrom", fromAccount, toBitcoinAddress, amount);
    }

    @Override
    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf) throws BitcoinException {
        return (String)this.query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf);
    }

    @Override
    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment) throws BitcoinException {
        return (String)this.query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf, comment);
    }

    @Override
    public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment, String commentTo) throws BitcoinException {
        return (String)this.query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf, comment, commentTo);
    }

    @Override
    public String sendMany(String fromAccount, List<TxOutput> outputs) throws BitcoinException {
        Map<String, BigDecimal> pOutputs = new LinkedHashMap();
        Iterator var5 = outputs.iterator();

        while(var5.hasNext()) {
            TxOutput txOutput = (TxOutput)var5.next();
            BigDecimal oldValue;
            if ((oldValue = (BigDecimal)pOutputs.put(txOutput.address(), txOutput.amount())) != null) {
                pOutputs.put(txOutput.address(), oldValue.add(txOutput.amount()));
            }
        }

        return (String)this.query("sendmany", fromAccount, pOutputs);
    }

    @Override
    public String sendMany(String fromAccount, List<TxOutput> outputs, int minConf) throws BitcoinException {
        Map<String, BigDecimal> pOutputs = new LinkedHashMap();
        Iterator var6 = outputs.iterator();

        while(var6.hasNext()) {
            TxOutput txOutput = (TxOutput)var6.next();
            BigDecimal oldValue;
            if ((oldValue = (BigDecimal)pOutputs.put(txOutput.address(), txOutput.amount())) != null) {
                pOutputs.put(txOutput.address(), oldValue.add(txOutput.amount()));
            }
        }

        return (String)this.query("sendmany", fromAccount, pOutputs, minConf);
    }

    @Override
    public String sendMany(String fromAccount, List<TxOutput> outputs, int minConf, String comment) throws BitcoinException {
        Map<String, BigDecimal> pOutputs = new LinkedHashMap();
        Iterator var7 = outputs.iterator();

        while(var7.hasNext()) {
            TxOutput txOutput = (TxOutput)var7.next();
            BigDecimal oldValue;
            if ((oldValue = (BigDecimal)pOutputs.put(txOutput.address(), txOutput.amount())) != null) {
                pOutputs.put(txOutput.address(), oldValue.add(txOutput.amount()));
            }
        }

        return (String)this.query("sendmany", fromAccount, pOutputs, minConf, comment);
    }

    @Override
    public String sendRawTransaction(String hex) {
        try{
            return (String)this.query("sendrawtransaction", hex);
        }catch (Exception e){
            e.printStackTrace();
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    @Override
    public String sendToAddress(String toAddress, double amount) throws BitcoinException {
        return (String)this.query("sendtoaddress", toAddress, amount);
    }

    @Override
    public String sendToAddress(String toAddress, double amount, String comment) throws BitcoinException {
        return (String)this.query("sendtoaddress", toAddress, amount, comment);
    }

    @Override
    public Boolean setTxFee(double amount) throws BitcoinException {
        return (Boolean)this.query("settxfee", amount);
    }

    @Override
    public String sendToAddress(String toAddress, double amount, String comment, String commentTo) throws BitcoinException {
        return (String)this.query("sendtoaddress", toAddress, amount, comment, commentTo);
    }

    @Override
    public String signMessage(String address, String message) throws BitcoinException {
        return (String)this.query("signmessage", address, message);
    }

    @Override
    public String signRawTransaction(String hex) throws BitcoinException {
        Map result = (Map)this.query("signrawtransaction", hex);
        if ((Boolean)result.get("complete")) {
            return (String)result.get("hex");
        } else {
            throw new BitcoinException("Incomplete");
        }
    }

    @Override
    public void stop() throws BitcoinException {
        this.query("stop");
    }

    @Override
    public AddressValidationResult validateAddress(String address) throws BitcoinException {
        final Map validationResult = (Map)this.query("validateaddress", address);
        return new AddressValidationResult() {
            @Override
            public boolean isValid() {
                return (Boolean)validationResult.get("isvalid");
            }

            @Override
            public String address() {
                return (String)validationResult.get("address");
            }

            @Override
            public boolean isMine() {
                return (Boolean)validationResult.get("ismine");
            }

            @Override
            public boolean isScript() {
                return (Boolean)validationResult.get("isscript");
            }

            @Override
            public String pubKey() {
                return (String)validationResult.get("pubkey");
            }

            @Override
            public boolean isCompressed() {
                return (Boolean)validationResult.get("iscompressed");
            }

            @Override
            public String account() {
                return (String)validationResult.get("account");
            }

            @Override
            public String toString() {
                return validationResult.toString();
            }
        };
    }

    @Override
    public boolean verifyMessage(String address, String signature, String message) throws BitcoinException {
        return (Boolean)this.query("verifymessage", address, signature, message);
    }

    @Override
    public List<ImportMultiResponse> importMulti(List<ImportMultiRequest> requests, boolean rescan) {
        try {
            // 准备请求数据
            List<Map<String, Object>> requestList = new ArrayList<>();
            for (ImportMultiRequest request : requests) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("scriptPubKey", Collections.singletonMap("address", request.getScriptPubKey().getAddress()));
                entry.put("timestamp", request.getTimestamp());
                entry.put("label", request.getLabel());
                entry.put("watchonly", request.isWatchonly());
                requestList.add(entry);
            }

            // 构建选项参数
            Map<String, Object> options = new HashMap<>();
            options.put("rescan", rescan);

            // 发起 JSON-RPC 调用
            List<Map<String, Object>> responseList = (List<Map<String, Object>>) query("importmulti", new Object[]{requestList, options});

            // 处理响应数据
            List<ImportMultiResponse> responses = new ArrayList<>();
            for (Map<String, Object> response : responseList) {
                ImportMultiResponse importResponse = new ImportMultiResponse();
                importResponse.setSuccess((Boolean) response.get("success"));
                if (response.containsKey("error")) {
                    Map<String, Object> error = (Map<String, Object>) response.get("error");
                    importResponse.setError((String) error.get("message"));
                }
                responses.add(importResponse);
            }
            return responses;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to import multiple addresses", e);
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }
    }

    public String transferRaw(String toAddress, BigDecimal transferAmount, String fromAddress, String privateKey) {
        //获取fromAddress的utxo
        List<Unspent> unspentList = listUnspent(fromAddress);
        if (unspentList == null || unspentList.size() == 0) {
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }

        // 估算交易大小 // 假设只有一个输出
        int estimatedSize = estimateTransactionSize(unspentList.size(), 1);

        //计算手续费
        Long fee = getTransferFee(estimatedSize);
        BtcRaw btcRaw = new BtcRaw();
        btcRaw.setPrivateKey(privateKey);
        btcRaw.setFee(fee);
        btcRaw.setRecevieAddr(toAddress);
        btcRaw.setFromAddr(fromAddress);
        btcRaw.setAmount(convertToSatoshis(transferAmount));

        List<UnSpentUtxo> unSpentUtxos = new ArrayList<>();
        for (Unspent unspent : unspentList) {
            UnSpentUtxo unSpentUtxo = new UnSpentUtxo();
            unSpentUtxo.setHash(unspent.txid());

            unSpentUtxo.setScript(unspent.scriptPubKey());
            unSpentUtxo.setTxN(unspent.vout());
            unSpentUtxo.setHeight(unspent.vout());

            unSpentUtxo.setValue(convertToSatoshis(unspent.amount()));
            unSpentUtxo.setAddress(unspent.address());
            unSpentUtxos.add(unSpentUtxo);
        }
        btcRaw.setUnUtxos(unSpentUtxos);
        String sign = coolSign(btcRaw);
        return sendRawTransaction(sign);
    }

    private class UnspentListWrapper extends ListMapWrapper<Unspent> {
        public UnspentListWrapper(List<Map> list) {
            super(list);
        }

        @Override
        protected Unspent wrap(final Map m) {
            return new Unspent() {

                @Override
                public String txid() {
                    return MapWrapper.mapStr(m, "txid");
                }

                @Override
                public int vout() {
                    return MapWrapper.mapInt(m, "vout");
                }

                @Override
                public String address() {
                    return MapWrapper.mapStr(m, "address");
                }

                @Override
                public String scriptPubKey() {
                    return MapWrapper.mapStr(m, "scriptPubKey");
                }

                @Override
                public String account() {
                    return MapWrapper.mapStr(m, "account");
                }

                @Override
                public BigDecimal amount() {
                    return MapWrapper.mapBigDecimal(m, "amount");
                }

                @Override
                public int confirmations() {
                    return MapWrapper.mapInt(m, "confirmations");
                }
            };
        }
    }

    private class TransactionsSinceBlockImpl implements TransactionsSinceBlock {
        public final List<Transaction> transactions;
        public final String lastBlock;

        public TransactionsSinceBlockImpl(Map r) {
            this.transactions = BitcoinRPCClient.this.new TransactionListMapWrapper((List)r.get("transactions"));
            this.lastBlock = (String)r.get("lastblock");
        }

        @Override
        public List<Transaction> transactions() {
            return this.transactions;
        }

        @Override
        public String lastBlock() {
            return this.lastBlock;
        }
    }

    private class TransactionListMapWrapper extends ListMapWrapper<Transaction> {
        public TransactionListMapWrapper(List<Map> list) {
            super(list);
        }

        @Override
        protected Transaction wrap(final Map m) {
            return new Transaction() {
                private RawTransaction raw = null;

                @Override
                public String account() {
                    return MapWrapper.mapStr(m, "account");
                }

                @Override
                public String address() {
                    return MapWrapper.mapStr(m, "address");
                }

                @Override
                public String category() {
                    return MapWrapper.mapStr(m, "category");
                }

                @Override
                public double amount() {
                    return MapWrapper.mapDouble(m, "amount");
                }

                @Override
                public double fee() {
                    return MapWrapper.mapDouble(m, "fee");
                }

                @Override
                public int confirmations() {
                    return MapWrapper.mapInt(m, "confirmations");
                }

                @Override
                public String blockHash() {
                    return MapWrapper.mapStr(m, "blockhash");
                }

                @Override
                public int blockIndex() {
                    return MapWrapper.mapInt(m, "blockindex");
                }

                @Override
                public Date blockTime() {
                    return MapWrapper.mapCTime(m, "blocktime");
                }

                @Override
                public String txId() {
                    return MapWrapper.mapStr(m, "txid");
                }

                @Override
                public Date time() {
                    return MapWrapper.mapCTime(m, "time");
                }

                @Override
                public Date timeReceived() {
                    return MapWrapper.mapCTime(m, "timereceived");
                }

                @Override
                public String comment() {
                    return MapWrapper.mapStr(m, "comment");
                }

                @Override
                public String commentTo() {
                    return MapWrapper.mapStr(m, "to");
                }

                @Override
                public RawTransaction raw() {
                    if (this.raw == null) {
                        try {
                            this.raw = BitcoinRPCClient.this.getRawTransaction(this.txId());
                        } catch (BitcoinException var2) {
                            throw new RuntimeException(var2);
                        }
                    }

                    return this.raw;
                }

                @Override
                public String toString() {
                    return m.toString();
                }
            };
        }
    }

    private static class ReceivedAddressListWrapper extends AbstractList<ReceivedAddress> {
        private final List<Map<String, Object>> wrappedList;

        public ReceivedAddressListWrapper(List<Map<String, Object>> wrappedList) {
            this.wrappedList = wrappedList;
        }

        @Override
        public ReceivedAddress get(int index) {
            final Map<String, Object> e = (Map)this.wrappedList.get(index);
            return new ReceivedAddress() {

                @Override
                public String address() {
                    return (String)e.get("address");
                }

                @Override
                public String account() {
                    return (String)e.get("account");
                }

                @Override
                public double amount() {
                    return ((Number)e.get("amount")).doubleValue();
                }

                @Override
                public int confirmations() {
                    return ((Number)e.get("confirmations")).intValue();
                }

                @Override
                public String toString() {
                    return e.toString();
                }
            };
        }

        @Override
        public int size() {
            return this.wrappedList.size();
        }
    }

    private class RawTransactionImpl extends MapWrapper implements RawTransaction {
        public RawTransactionImpl(Map<String, Object> tx) {
            super(tx);
        }

        @Override
        public String hex() {
            return this.mapStr("hex");
        }

        @Override
        public String txId() {
            return this.mapStr("txid");
        }

        @Override
        public int version() {
            return this.mapInt("version");
        }

        @Override
        public long lockTime() {
            return this.mapLong("locktime");
        }

        @Override
        public List<In> vIn() {
            final List<Map<String, Object>> vIn = (List)this.m.get("vin");
            return new AbstractList<In>() {

                @Override
                public In get(int index) {
                    return RawTransactionImpl.this.new InImpl((Map)vIn.get(index));
                }

                @Override
                public int size() {
                    return vIn.size();
                }
            };
        }

        @Override
        public List<Out> vOut() {
            final List<Map<String, Object>> vOut = (List)this.m.get("vout");
            return new AbstractList<Out>() {

                @Override
                public Out get(int index) {
                    return RawTransactionImpl.this.new OutImpl((Map)vOut.get(index));
                }

                @Override
                public int size() {
                    return vOut.size();
                }
            };
        }

        @Override
        public String blockHash() {
            return this.mapStr("blockhash");
        }

        @Override
        public int confirmations() {
            return this.mapInt("confirmations");
        }

        @Override
        public Date time() {
            return this.mapCTime("time");
        }

        @Override
        public Date blocktime() {
            return this.mapCTime("blocktime");
        }

        private class OutImpl extends MapWrapper implements Out {
            public OutImpl(Map m) {
                super(m);
            }

            @Override
            public double value() {
                return this.mapDouble("value");
            }

            @Override
            public int n() {
                return this.mapInt("n");
            }

            @Override
            public ScriptPubKey scriptPubKey() {
                return new ScriptPubKeyImpl((Map)this.m.get("scriptPubKey"));
            }

            @Override
            public TxInput toInput() {
                return new BasicTxInput(this.transaction().txId(), this.n());
            }

            @Override
            public RawTransaction transaction() {
                return RawTransactionImpl.this;
            }

            private class ScriptPubKeyImpl extends MapWrapper implements ScriptPubKey {
                public ScriptPubKeyImpl(Map m) {
                    super(m);
                }

                @Override
                public String asm() {
                    return this.mapStr("asm");
                }

                @Override
                public String hex() {
                    return this.mapStr("hex");
                }

                @Override
                public int reqSigs() {
                    return this.mapInt("reqSigs");
                }

                @Override
                public String type() {
                    return this.mapStr(this.type());
                }

                @Override
                public List<String> addresses() {
                    return (List)this.m.get("addresses");
                }

                @Override
                public String address() {
                    return this.mapStr("address");
                }
            }
        }

        private class InImpl extends MapWrapper implements In {
            public InImpl(Map m) {
                super(m);
            }

            @Override
            public String txid() {
                return this.mapStr("txid");
            }

            @Override
            public int vout() {
                return this.mapInt("vout");
            }

            @Override
            public Map<String, Object> scriptSig() {
                return (Map)this.m.get("scriptSig");
            }

            @Override
            public long sequence() {
                return this.mapLong("sequence");
            }

            @Override
            public RawTransaction getTransaction() {
                try {
                    return BitcoinRPCClient.this.getRawTransaction(this.mapStr("txid"));
                } catch (BitcoinException var2) {
                    throw new RuntimeException(var2);
                }
            }

            @Override
            public Out getTransactionOutput() {
                return (Out)this.getTransaction().vOut().get(this.mapInt("vout"));
            }
        }
    }

    private class PeerInfoMapWrapper extends MapWrapper implements PeerInfo {
        public PeerInfoMapWrapper(Map m) {
            super(m);
        }

        @Override
        public String addr() {
            return this.mapStr("addr");
        }

        @Override
        public String services() {
            return this.mapStr("services");
        }

        @Override
        public int lastsend() {
            return this.mapInt("lastsend");
        }

        @Override
        public int lastrecv() {
            return this.mapInt("lastrecv");
        }

        @Override
        public int bytessent() {
            return this.mapInt("bytessent");
        }

        @Override
        public int bytesrecv() {
            return this.mapInt("bytesrecv");
        }

        @Override
        public int blocksrequested() {
            return this.mapInt("blocksrequested");
        }

        @Override
        public Date conntime() {
            return this.mapCTime("conntime");
        }

        @Override
        public int version() {
            return this.mapInt("version");
        }

        @Override
        public String subver() {
            return this.mapStr("subver");
        }

        @Override
        public boolean inbound() {
            return this.mapBool("inbound");
        }

        @Override
        public int startingheight() {
            return this.mapInt("startingheight");
        }

        @Override
        public int banscore() {
            return this.mapInt("banscore");
        }
    }

    private class MiningInfoMapWrapper extends MapWrapper implements MiningInfo {
        public MiningInfoMapWrapper(Map m) {
            super(m);
        }

        @Override
        public int blocks() {
            return this.mapInt("blocks");
        }

        @Override
        public int currentblocksize() {
            return this.mapInt("currentblocksize");
        }

        @Override
        public int currentblocktx() {
            return this.mapInt("currentblocktx");
        }

        @Override
        public double difficulty() {
            return this.mapDouble("difficulty");
        }

        @Override
        public String errors() {
            return this.mapStr("errors");
        }

        @Override
        public int genproclimit() {
            return this.mapInt("genproclimit");
        }

        @Override
        public double networkhashps() {
            return this.mapDouble("networkhashps");
        }

        @Override
        public int pooledtx() {
            return this.mapInt("pooledtx");
        }

        @Override
        public boolean testnet() {
            return this.mapBool("testnet");
        }

        @Override
        public String chain() {
            return this.mapStr("chain");
        }

        @Override
        public boolean generate() {
            return this.mapBool("generate");
        }
    }

    private class InfoMapWrapper extends MapWrapper implements Info {
        public InfoMapWrapper(Map m) {
            super(m);
        }

        @Override
        public int version() {
            return this.mapInt("version");
        }

        @Override
        public int protocolversion() {
            return this.mapInt("protocolversion");
        }

        @Override
        public int walletversion() {
            return this.mapInt("walletversion");
        }

        @Override
        public double balance() {
            return this.mapDouble("balance");
        }

        @Override
        public int blocks() {
            return this.mapInt("blocks");
        }

        @Override
        public int timeoffset() {
            return this.mapInt("timeoffset");
        }

        @Override
        public int connections() {
            return this.mapInt("connections");
        }

        @Override
        public String proxy() {
            return this.mapStr("proxy");
        }

        @Override
        public double difficulty() {
            return this.mapDouble("difficulty");
        }

        @Override
        public boolean testnet() {
            return this.mapBool("testnet");
        }

        @Override
        public int keypoololdest() {
            return this.mapInt("keypoololdest");
        }

        @Override
        public int keypoolsize() {
            return this.mapInt("keypoolsize");
        }

        @Override
        public int unlocked_until() {
            return this.mapInt("unlocked_until");
        }

        @Override
        public double paytxfee() {
            return this.mapDouble("paytxfee");
        }

        @Override
        public double relayfee() {
            return this.mapDouble("relayfee");
        }

        @Override
        public String errors() {
            return this.mapStr("errors");
        }
    }

    private class BlockMapWrapper extends MapWrapper implements Block {
        public BlockMapWrapper(Map m) {
            super(m);
        }

        @Override
        public String hash() {
            return this.mapStr("hash");
        }

        @Override
        public int confirmations() {
            return this.mapInt("confirmations");
        }

        @Override
        public int size() {
            return this.mapInt("size");
        }

        @Override
        public int height() {
            return this.mapInt("height");
        }

        @Override
        public int version() {
            return this.mapInt("version");
        }

        @Override
        public String merkleRoot() {
            return this.mapStr("");
        }

        @Override
        public List<String> tx() {
            return (List)this.m.get("tx");
        }

        @Override
        public Date time() {
            return this.mapCTime("time");
        }

        @Override
        public long nonce() {
            return this.mapLong("nonce");
        }

        @Override
        public String bits() {
            return this.mapStr("bits");
        }

        @Override
        public double difficulty() {
            return this.mapDouble("difficulty");
        }

        @Override
        public String previousHash() {
            return this.mapStr("previousblockhash");
        }

        @Override
        public String nextHash() {
            return this.mapStr("nextblockhash");
        }

        @Override
        public Block previous() throws BitcoinException {
            return !this.m.containsKey("previousblockhash") ? null : BitcoinRPCClient.this.getBlock(this.previousHash());
        }

        @Override
        public Block next() throws BitcoinException {
            return !this.m.containsKey("nextblockhash") ? null : BitcoinRPCClient.this.getBlock(this.nextHash());
        }
    }

    /**
     * 获取最快确认的推荐费用（单位：聪/字节）
     * @return
     */
    public long getTransferFee(int transactionSize) {
        String body = HttpUtil.createGet("https://mempool.space/api/v1/fees/recommended")
                .setHttpProxy("172.20.6.59", 3128)
                .execute().body();
        JSONObject jsonObject = JSONObject.parseObject(body);
        long feePerByte = jsonObject.getLong("fastestFee");
        // 计算总手续费
        return feePerByte * transactionSize;
    }

    // 估计交易大小的方法，这里只是一个示例
    public int estimateTransactionSize(int inputsCount, int outputsCount) {
        // 每个输入约 180 字节，输出约 34 字节，加上 10 字节的基础交易框架
        return inputsCount * 180 + outputsCount * 34 + 10;
    }

    public String coolSign(BtcRaw btcRaw){

        log.info("coolSign btcRaw:{}",btcRaw);
        RequestData requestData = CoolWalletFormatParam.formatParma(btcRaw, Chain.BTC);
        String requestCoolWalletParam = JSONObject.toJSONString(requestData);
        log.info("requestCoolWalletParam:{}",requestCoolWalletParam);
        String coolWalletResult = HttpUtil.createPost(coolWalletUrl+"/sign/btc")
                .header("Content-Type", "application/json")
                .body(requestCoolWalletParam)
                .execute().body();

        CommonResult commonResult = JSONObject.parseObject(coolWalletResult, CommonResult.class);
        log.info("coolWalletResult:{}",coolWalletResult);
        if(commonResult.getCode() == 0) {
            String signReulst = (String) commonResult.getData();
            return signReulst;
        }else{
            throw new WalletException.Builder()
                    .code(WalletResult.UNKNOWN_ERROR.getCode())
                    .msg(WalletResult.UNKNOWN_ERROR.getMessage())
                    .build();
        }

    }

    private final BigDecimal SATOSHIS_PER_BITCOIN = new BigDecimal(100000000);

    public long convertToSatoshis(BigDecimal bitcoins) {
        return bitcoins.multiply(SATOSHIS_PER_BITCOIN).longValueExact();
    }

    public BigDecimal convertToBitcoins(long satoshis) {
        return new BigDecimal(satoshis).divide(SATOSHIS_PER_BITCOIN);
    }
}
